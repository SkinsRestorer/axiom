package net.skinsrestorer.axiom;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class AxiomConfigurationSection {
    protected final Yaml yaml;
    protected MappingNode rootNode;

    //
    // Accessors
    //
    public Optional<Node> getNode(String path) {
        String[] parts = path.split("\\.");
        AxiomConfigurationSection node = this;
        int i = 0;
        for (String part : parts) {
            Optional<Node> subNode = node.getDirectSubNode(part);

            if (i == parts.length - 1) {
                return subNode;
            } else if (subNode.isPresent() && subNode.get() instanceof MappingNode) {
                node = new AxiomConfigurationSection(yaml, (MappingNode) subNode.get());
            } else {
                return Optional.empty();
            }

            i++;
        }

        return Optional.empty();
    }

    public List<String> getKeys() {
        List<String> keys = new ArrayList<>();
        for (NodeTuple tuple : rootNode.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode) {
                ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                keys.add(keyNode.getValue());
            }
        }
        return keys;
    }

    protected Optional<Node> getKeyNodeOfSubNode(String name) {
        for (NodeTuple tuple : rootNode.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode) {
                ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                if (keyNode.getValue().equals(name)) {
                    return Optional.of(keyNode);
                }
            }
        }
        return Optional.empty();
    }

    protected Optional<Node> getDirectSubNode(String name) {
        for (NodeTuple tuple : rootNode.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode) {
                ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                if (keyNode.getValue().equals(name)) {
                    return Optional.ofNullable(tuple.getValueNode());
                }
            }
        }
        return Optional.empty();
    }

    protected void setDirectSubNode(String name, Node setKeyNode, Node setValueNode) {
        if (rootNode.getValue().stream().map(NodeTuple::getKeyNode).anyMatch(node1 -> node1 instanceof ScalarNode && ((ScalarNode) node1).getValue().equals(name))) {
            rootNode.getValue().replaceAll(
                    tuple -> {
                        if (tuple.getKeyNode() instanceof ScalarNode) {
                            ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                            if (keyNode.getValue().equals(name)) {
                                if (setKeyNode != null) {
                                    return new NodeTuple(setKeyNode, setValueNode);
                                } else {
                                    return new NodeTuple(keyNode, setValueNode);
                                }
                            }
                        }
                        return tuple;
                    }
            );
        } else {
            if (setKeyNode != null) {
                rootNode.getValue().add(new NodeTuple(setKeyNode, setValueNode));
            } else {
                rootNode.getValue().add(createTuple(name, setValueNode));
            }
        }
    }

    public AxiomConfigurationSection getSection(String path) {
        Optional<Node> node = getNode(path);
        if (node.isPresent() && node.get() instanceof MappingNode) {
            return new AxiomConfigurationSection(yaml, (MappingNode) node.get());
        } else {
            return null;
        }
    }

    public String getString(String path) {
        Optional<Node> node = getNode(path);
        if (node.isPresent() && node.get() instanceof ScalarNode) {
            return ((ScalarNode) node.get()).getValue();
        } else {
            return null;
        }
    }

    public Integer getInt(String path) throws NumberFormatException {
        String value = getString(path);
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            return null;
        }
    }

    public Boolean getBoolean(String path) {
        String value = getString(path);
        if (value != null) {
            return Boolean.parseBoolean(value);
        } else {
            return null;
        }
    }

    public List<String> getStringList(String path) {
        Optional<Node> node = getNode(path);
        if (node.isPresent() && node.get() instanceof SequenceNode) {
            List<String> list = new ArrayList<>();
            SequenceNode sequenceNode = (SequenceNode) node.get();
            for (Node valueNode : sequenceNode.getValue()) {
                if (valueNode instanceof ScalarNode) {
                    ScalarNode scalarNode = (ScalarNode) valueNode;
                    list.add(scalarNode.getValue());
                }
            }
            return list;
        } else {
            return null;
        }
    }

    public void set(String path, Object value) {
        String[] parts = path.split("\\.");
        String target = parts[parts.length - 1];
        String parentPath = path.substring(0, path.length() - target.length());

        NodeTuple keyNode = null;
        Optional<Node> parentNode = getNode(parentPath);
        if (parentNode.isPresent() && parentNode.get() instanceof MappingNode) {
            MappingNode mappingNode = (MappingNode) parentNode.get();
            for (NodeTuple tuple : mappingNode.getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    ScalarNode scalarKeyNode = (ScalarNode) tuple.getKeyNode();
                    if (scalarKeyNode.getValue().equals(target)) {
                        keyNode = tuple;
                    }
                }
            }
        }

        set(path, value == null ? null : createTuple(target, value, keyNode));
    }

    private void set(String path, NodeTuple value) {
        String[] parts = path.split("\\.");
        try {
            MappingNode node = rootNode;
            int i = 0;
            for (String part : parts) {
                if (i < parts.length - 1) {
                    Optional<NodeTuple> optionalTuple = node.getValue().stream().filter(tuple -> {
                        if (tuple.getKeyNode() instanceof ScalarNode) {
                            ScalarNode scalarKeyNode = (ScalarNode) tuple.getKeyNode();
                            return scalarKeyNode.getValue().equals(part);
                        }
                        return false;
                    }).findFirst();

                    if (optionalTuple.isPresent()) {
                        if (optionalTuple.get().getValueNode() instanceof MappingNode) {
                            node = (MappingNode) optionalTuple.get().getValueNode();
                        } else {
                            throw new Exception("Invalid node type");
                        }
                    } else {
                        MappingNode newMapping = (MappingNode) yaml.represent(Collections.emptyMap());
                        node.getValue().add(createTuple(part, newMapping));
                        node = newMapping;
                    }
                } else {
                    if (value != null) {
                        if (node.getValue().stream().anyMatch(nodeTuple -> {
                            if (nodeTuple.getKeyNode() instanceof ScalarNode) {
                                ScalarNode scalarKeyNode = (ScalarNode) nodeTuple.getKeyNode();
                                return scalarKeyNode.getValue().equals(part);
                            }
                            return false;
                        })) {
                            node.getValue().replaceAll(nodeTuple -> {
                                if (nodeTuple.getKeyNode() instanceof ScalarNode) {
                                    ScalarNode scalarKeyNode = (ScalarNode) nodeTuple.getKeyNode();
                                    if (scalarKeyNode.getValue().equals(part)) {
                                        setComments(value.getKeyNode(), nodeTuple.getKeyNode(), false);
                                        setComments(value.getValueNode(), nodeTuple.getValueNode(), false);
                                        return value;
                                    }
                                }
                                return nodeTuple;
                            });
                        } else {
                            node.getValue().add(value);
                        }
                    } else {
                        node.getValue().removeIf(tuple -> {
                            if (tuple.getKeyNode() instanceof ScalarNode) {
                                ScalarNode scalarKeyNode = (ScalarNode) tuple.getKeyNode();
                                return scalarKeyNode.getValue().equals(part);
                            }
                            return false;
                        });
                    }
                }

                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void merge(AxiomConfigurationSection defaultConfig) {
        merge(defaultConfig, false, true, false);
    }

    public void merge(AxiomConfigurationSection defaultConfig, boolean overWriteComments, boolean overWriteInvalid, boolean overWrite) {
        setComments(rootNode, defaultConfig.rootNode, overWriteComments);

        for (String key : defaultConfig.getKeys()) {
            Node defaultNode = defaultConfig.getDirectSubNode(key).orElseThrow(() -> new IllegalStateException("Node not found"));
            Node defaultParentNode = defaultConfig.getKeyNodeOfSubNode(key).orElseThrow(() -> new IllegalStateException("Node not found"));
            Optional<Node> currentNode = getDirectSubNode(key);

            if (!currentNode.isPresent() || overWrite) {
                setDirectSubNode(key, defaultParentNode, defaultNode);
            } else {
                setComments(currentNode.get(), defaultNode, overWriteComments);

                if (defaultNode instanceof MappingNode) {
                    if (currentNode.get() instanceof MappingNode) {
                        AxiomConfigurationSection childDefaultSection = new AxiomConfigurationSection(yaml, (MappingNode) defaultNode);
                        AxiomConfigurationSection childCurrentSection = new AxiomConfigurationSection(yaml, (MappingNode) currentNode.get());

                        childCurrentSection.merge(childDefaultSection, overWriteComments, overWriteInvalid, false);
                    } else if (overWriteInvalid) {
                        setDirectSubNode(key, defaultParentNode, defaultNode);
                    }
                }
            }
        }
    }

    protected static void setComments(Node currentNode, Node defaultNode, boolean overWrite) {
        if (overWrite) {
            currentNode.setBlockComments(defaultNode.getBlockComments());
            currentNode.setInLineComments(defaultNode.getInLineComments());
            currentNode.setEndComments(defaultNode.getEndComments());
        } else {
            if (currentNode.getBlockComments() == null) {
                currentNode.setBlockComments(defaultNode.getBlockComments());
            }
            if (currentNode.getInLineComments() == null) {
                currentNode.setInLineComments(defaultNode.getInLineComments());
            }
            if (currentNode.getEndComments() == null) {
                currentNode.setEndComments(defaultNode.getEndComments());
            }
        }
    }

    private NodeTuple createTuple(Node key, Object value, @Nullable NodeTuple previousTuple) {
        if (value instanceof String) {
            Optional<Number> optionalNumber = parseNumber((String) value);

            if (optionalNumber.isPresent()) {
                value = optionalNumber.get();
            } else {
				Optional<Boolean> optionalBoolean = parseBoolean((String) value);

				if (optionalBoolean.isPresent()) {
					value = optionalBoolean.get();
				}
			}
        }

        return createTuple(key, yaml.represent(value), previousTuple);
    }

    private NodeTuple createTuple(Object key, Node value) {
        return createTuple(yaml.represent(key), value, null);
    }

    private NodeTuple createTuple(Node key, Node value, @Nullable NodeTuple previousTuple) {
        if (previousTuple != null) {
            setComments(key, previousTuple.getKeyNode(), true);
            setComments(value, previousTuple.getValueNode(), true);
        }

        if (value instanceof SequenceNode) {
            SequenceNode sequenceNode = (SequenceNode) value;
            int i = 0;
            for (Node node : new ArrayList<>(sequenceNode.getValue())) {
                if (node instanceof ScalarNode) {
                    ScalarNode scalarNode = (ScalarNode) node;

                    sequenceNode.getValue().set(i,
                            new ScalarNode(
                                    scalarNode.getTag(),
                                    scalarNode.getValue(),
                                    scalarNode.getStartMark(),
                                    scalarNode.getEndMark(),
                                    DumperOptions.ScalarStyle.DOUBLE_QUOTED));
                }
                i++;
            }
        }

        return new NodeTuple(key, value);
    }

    private NodeTuple createTuple(Object key, Object value, @Nullable NodeTuple previousTuple) {
        return createTuple(yaml.represent(key), value, previousTuple);
    }

    private static Optional<Number> parseNumber(String str) {
        try {
            if (str != null && !str.isEmpty()) {
                str = str.trim();
                if (str.matches("[+-]?\\d+")) {
                    return Optional.of(Integer.parseInt(str));
                } else if (str.matches("[+-]?\\d+\\.\\d+")) {
                    return Optional.of(Double.parseDouble(str));
                } else if (str.matches("[+-]?\\d+\\.\\d+[eE][+-]?\\d+")) {
                    return Optional.of(Double.parseDouble(str));
                }
            }

            return Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> parseBoolean(String str) {
        if (str != null && !str.isEmpty()) {
            str = str.trim();
            if (str.matches("true")) {
                return Optional.of(true);
            } else if (str.matches("false")) {
                return Optional.of(false);
            }
        }

        return Optional.empty();
    }
}
