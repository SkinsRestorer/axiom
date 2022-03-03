package net.skinsrestorer.axiom;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class AxiomConfigurationSection {
    protected final Yaml yaml;
    protected MappingNode rootNode;

    //
    // Accessors
    //
    public Node getNode(String path) {
        String[] parts = path.split("\\.");
        try {
            Node node = rootNode;
            int i = 0;
            for (String part : parts) {
                if (i == parts.length) {
                    return node;
                }

                if (node instanceof MappingNode) {
                    MappingNode mappingNode = (MappingNode) node;
                    for (NodeTuple tuple : mappingNode.getValue()) {
                        if (tuple.getKeyNode() instanceof ScalarNode) {
                            ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();

                            if (keyNode.getValue().equals(part)) {
                                node = tuple.getValueNode();
                                break;
                            }
                        } else {
                            return null;
                        }
                    }
                } else {
                    return null;
                }

                i++;
            }

            return node;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    public AxiomConfigurationSection getSection(String path) {
        Node node = getNode(path);
        if (node instanceof MappingNode) {
            return new AxiomConfigurationSection(yaml, (MappingNode) node);
        } else {
            return null;
        }
    }

    public String getString(String path) {
        Node node = getNode(path);
        if (node instanceof ScalarNode) {
            return ((ScalarNode) node).getValue();
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
        Node node = getNode(path);
        if (node instanceof SequenceNode) {
            List<String> list = new ArrayList<>();
            SequenceNode sequenceNode = (SequenceNode) node;
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
        System.out.println("Setting " + target + " to " + value);
        String parentPath = path.substring(0, path.length() - target.length());

        NodeTuple keyNode = null;
        Node parentNode = getNode(parentPath);
        if (parentNode instanceof MappingNode) {
            MappingNode mappingNode = (MappingNode) parentNode;
            for (NodeTuple tuple : mappingNode.getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    ScalarNode scalarKeyNode = (ScalarNode) tuple.getKeyNode();
                    if (scalarKeyNode.getValue().equals(target)) {
                        keyNode = tuple;
                    }
                }
            }
        }

        set(path, createTuple(target, value, keyNode));
    }

    public void set(String path, NodeTuple value) {
        boolean destroy = value == null;
        String[] parts = path.split("\\.");
        try {
            MappingNode node = rootNode;
            int i = 0;
            for (String part : parts) {
                if (i == parts.length - 1) {
                    List<NodeTuple> nodeValue = node.getValue();

                    int z = 0;
                    for (NodeTuple tuple : new ArrayList<>(nodeValue)) {
                        if (tuple.getKeyNode() instanceof ScalarNode) {
                            ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                            if (keyNode.getValue().equals(part)) {
                                if (destroy) {
                                    nodeValue.remove(z);
                                } else {
                                    nodeValue.set(z, value);
                                }

                                return;
                            }
                        }
                        z++;
                    }

                    nodeValue.add(value);
                    return;
                }

                int x = 0;
                for (NodeTuple tuple : node.getValue()) {
                    if (tuple.getKeyNode() instanceof ScalarNode) {
                        if (tuple.getValueNode() instanceof MappingNode) {
                            ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();

                            if (keyNode.getValue().equals(part)) {
                                node = (MappingNode) tuple.getValueNode();
                                break;
                            }
                        }
                    }
                    x++;
                }

                if (x == node.getValue().size()) {
                    MappingNode newMapping = (MappingNode) yaml.represent(Collections.emptyMap());
                    node.getValue().add(createTuple(part, newMapping));
                    node = newMapping;
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setComments(Node currentNode, Node defaultNode, boolean overWrite) {
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
            if (NumberUtils.isParsable((String) value)) {
                value = NumberUtils.createNumber((String) value);
            } else if (BooleanUtils.toBooleanObject((String) value) != null) {
                value = BooleanUtils.toBoolean((String) value);
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
}
