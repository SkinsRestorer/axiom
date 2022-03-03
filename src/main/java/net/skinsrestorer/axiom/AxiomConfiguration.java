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
import java.util.*;

public class AxiomConfiguration extends AxiomConfigurationSection{
    public AxiomConfiguration() {
        this(2, 2);
    }

    public AxiomConfiguration(int indent, int indicatorIdent) {
        super(generateYAML(indent, indicatorIdent), null);
        rootNode = (MappingNode) yaml.represent(new HashMap<>());
    }

    private static Yaml generateYAML(int indent, int indicatorIdent) {
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

        return new Yaml(constructor, representer, dumper, loaderOptions);
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

    public void load(String string) throws IOException {
        try (StringReader reader = new StringReader(string)) {
            load(reader);
        }
    }

    public void load(Reader reader) throws IOException {
        try {
            rootNode = (MappingNode) yaml.compose(reader);
        } catch (Exception e) {
            throw new InvalidObjectException("Invalid configuration file");
        }
    }

    public void save(Path path) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
                yaml.serialize(rootNode, writer);
            }
        }
    }

    public String saveToString() {
        StringWriter writer = new StringWriter();
        yaml.serialize(rootNode, writer);
        return writer.toString();
    }

    public void mergeDefault(AxiomConfiguration defaultConfig) {
        mergeDefault(defaultConfig, false, false);
    }

    public void mergeDefault(AxiomConfiguration defaultConfig, boolean overWriteComments, boolean overWrite) {
        setComments(rootNode, defaultConfig.rootNode, overWriteComments);

        Map<String, NodeTuple> defaultNodes = recursivelyGetAllNodes(defaultConfig.rootNode);
        Map<String, NodeTuple> currentNodes = recursivelyGetAllNodes(rootNode);

        for (Map.Entry<String, NodeTuple> entry : defaultNodes.entrySet()) {
            if (!currentNodes.containsKey(entry.getKey()) || overWrite) {
                if (entry.getValue().getValueNode() instanceof SequenceNode
                        || entry.getValue().getValueNode() instanceof ScalarNode) {
                    set(entry.getKey(), entry.getValue());
                }
            }
        }

        currentNodes = recursivelyGetAllNodes(rootNode);
        for (Map.Entry<String, NodeTuple> entry : defaultNodes.entrySet()) {
            if (currentNodes.containsKey(entry.getKey())) {
                NodeTuple currentNode = currentNodes.get(entry.getKey());
                setComments(currentNode.getKeyNode(), entry.getValue().getKeyNode(), overWriteComments);
                setComments(currentNode.getValueNode(), entry.getValue().getValueNode(), overWriteComments);
            }
        }
    }

    public Map<String, NodeTuple> recursivelyGetAllNodes(Node node) {
        return recursivelyGetAllNodes(node, "");
    }

    private Map<String, NodeTuple> recursivelyGetAllNodes(Node node, String prefix) {
        Map<String, NodeTuple> nodes = new LinkedHashMap<>();

        if (!prefix.isEmpty())
            prefix += ".";

        if (node instanceof MappingNode) {
            MappingNode mapping = (MappingNode) node;
            for (NodeTuple tuple : mapping.getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    ScalarNode key = (ScalarNode) tuple.getKeyNode();
                    String keyString = prefix + key.getValue();

                    nodes.put(keyString, tuple);

                    nodes.putAll(recursivelyGetAllNodes(tuple.getValueNode(), keyString));
                }
            }
        }

        return nodes;
    }
}
