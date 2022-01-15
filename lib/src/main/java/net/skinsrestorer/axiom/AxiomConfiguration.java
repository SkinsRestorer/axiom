package net.skinsrestorer.axiom;

import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AxiomConfiguration {
    private final Yaml yaml;
    public MappingNode config;

    public AxiomConfiguration() {
        this(2, 2);
    }

    public AxiomConfiguration(int indent, int indicatorIdent) {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true);
        DumperOptions dumper = new DumperOptions();
        dumper.setProcessComments(true);
        dumper.setIndent(indent);
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setIndicatorIndent(indicatorIdent);
        dumper.setIndentWithIndicator(true);
        dumper.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        Constructor constructor = new Constructor(loaderOptions);
        Representer representer = new Representer();
        yaml = new Yaml(constructor, representer, dumper, loaderOptions);
        config = (MappingNode) yaml.represent(new HashMap<>());
    }

    //
    // Loaders and savers
    //
    public void load(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            load(input);
        }
    }

    public void load(InputStream input) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(input)) {
            load(reader);
        }
    }

    public void load(Reader reader) throws IOException {
        try {
            config = (MappingNode) yaml.compose(reader);
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid configuration file");
        }
    }

    public void save(Path path) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
                yaml.dump(config, writer);
            }
        }
    }

    public void mergeDefault(AxiomConfiguration defaultConfig) {
        mergeDefault(defaultConfig, false, false);
    }

    public void mergeDefault(AxiomConfiguration defaultConfig, boolean overWriteComments, boolean overWrite) {
        Map<String, Node> defaultNodes = recursivelyGetAllNodes(defaultConfig.config);
        Map<String, Node> currentNodes = recursivelyGetAllNodes(config);

        for (Map.Entry<String, Node> entry : defaultNodes.entrySet()) {
            if (!currentNodes.containsKey(entry.getKey()) || overWrite) {
                if (entry.getValue() instanceof SequenceNode) {
                    set(entry.getKey(), defaultConfig.getStringList(entry.getKey()));
                } else if (entry.getValue() instanceof ScalarNode) {
                    ScalarNode seq = (ScalarNode) entry.getValue();
                    set(entry.getKey(), seq.getValue());
                }
            }
        }

        if (overWriteComments) {
            currentNodes = recursivelyGetAllNodes(config);
            for (Map.Entry<String, Node> entry : defaultNodes.entrySet()) {
                if (currentNodes.containsKey(entry.getKey())) {
                    currentNodes.get(entry.getKey()).setBlockComments(entry.getValue().getBlockComments());
                    currentNodes.get(entry.getKey()).setInLineComments(entry.getValue().getInLineComments());
                    currentNodes.get(entry.getKey()).setEndComments(entry.getValue().getEndComments());
                }
            }
        }
    }

    public Map<String, Node> recursivelyGetAllNodes(Node node) {
        return recursivelyGetAllNodes(node, "");
    }

    private Map<String, Node> recursivelyGetAllNodes(Node node, String prefix) {
        Map<String, Node> nodes = new LinkedHashMap<>();

        if (!prefix.isEmpty())
            prefix += ".";

        if (node instanceof MappingNode) {
            MappingNode mapping = (MappingNode) node;
            for (NodeTuple tuple : mapping.getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    ScalarNode key = (ScalarNode) tuple.getKeyNode();
                    String keyString = prefix + key.getValue();

                    nodes.put(keyString, tuple.getValueNode());

                    nodes.putAll(recursivelyGetAllNodes(tuple.getValueNode(), keyString));
                }
            }
        }

        return nodes;
    }

    public String saveToString() {
        StringWriter writer = new StringWriter();
        yaml.serialize(config, writer);
        return writer.toString();
    }

    //
    // Accessors
    //
    public Node getNode(String path) {
        String[] parts = path.split("\\.");
        try {
            Node node = config;
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
        boolean destroy = value == null;
        String[] parts = path.split("\\.");
        try {
            MappingNode node = config;
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
                                    nodeValue.set(z, createTuple(part, value, tuple));
                                }

                                return;
                            }
                        }
                        z++;
                    }


                    nodeValue.add(createTuple(part, value, null));
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
                    MappingNode newMapping = (MappingNode) yaml.represent(new HashMap<>());
                    node.getValue().add(createTuple(part, newMapping, null));
                    node = newMapping;
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private NodeTuple createTuple(Object key, Object value, @Nullable NodeTuple previousTuple) {
        Node keyNode = yaml.represent(key);
        keyNode.setBlockComments(previousTuple != null ? previousTuple.getKeyNode().getBlockComments() : null);
        keyNode.setInLineComments(previousTuple != null ? previousTuple.getKeyNode().getInLineComments() : null);
        keyNode.setEndComments(previousTuple != null ? previousTuple.getKeyNode().getEndComments() : null);

        if (value instanceof String) {
            if (NumberUtils.isParsable((String) value)) {
                value = NumberUtils.createNumber((String) value);
            }
        }

        Node valueNode = yaml.represent(value);
        valueNode.setBlockComments(previousTuple != null ? previousTuple.getValueNode().getBlockComments() : null);
        valueNode.setInLineComments(previousTuple != null ? previousTuple.getValueNode().getInLineComments() : null);
        valueNode.setEndComments(previousTuple != null ? previousTuple.getValueNode().getEndComments() : null);

        if (valueNode instanceof SequenceNode) {
            SequenceNode sequenceNode = (SequenceNode) valueNode;
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

        return new NodeTuple(keyNode, valueNode);
    }
}
