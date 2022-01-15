package net.skinsrestorer.axiom;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AxiomConfiguration {
    private final Yaml yaml;
    private MappingNode config;

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

    public void mergeDefault(AxiomConfiguration defaultConfig, boolean overWriteComments) {
        defaultConfig.config.getValue().forEach(tuple -> {

        });
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
                                    nodeValue.set(z, createTuple(part, value));
                                }

                                return;
                            }
                        }
                        z++;
                    }


                    nodeValue.add(createTuple(part, value));
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
                    node.getValue().add(createTuple(part, newMapping));
                    node = newMapping;
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NodeTuple createTuple(Object key, Object value) {
        Node valueNode = yaml.represent(value);
        if (valueNode instanceof SequenceNode) {
            int i = 0;
            for (Node node : new ArrayList<>(((SequenceNode) valueNode).getValue())) {
                if (node instanceof ScalarNode) {
                    ScalarNode scalarNode = (ScalarNode) node;

                    ((SequenceNode) valueNode).getValue().set(i,
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
        return new NodeTuple(yaml.represent(key), valueNode);
    }
}
