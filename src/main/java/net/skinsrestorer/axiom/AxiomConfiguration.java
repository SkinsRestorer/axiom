package net.skinsrestorer.axiom;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AxiomConfiguration extends AxiomConfigurationSection{
    public AxiomConfiguration() {
        this(2, 2);
    }

    public AxiomConfiguration(int indent, int indicatorIdent) {
        super(generateYAML(indent, indicatorIdent), null);
        rootNode = (MappingNode) yaml.represent(Collections.emptyMap());
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
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
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
            try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
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

        Map<String, NodeTuple> defaultValues = defaultConfig.getAllValueNodes();
        Map<String, NodeTuple> currentValues = getAllValueNodes();

        for (Map.Entry<String, NodeTuple> entry : defaultValues.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!currentValues.containsKey(key) || overWrite) {
                set(key, value);
            }

            // System out it
            System.out.println("Key: " + key + " Value: " + value);
        }
        System.out.println(defaultConfig.rootNode.getValue().get(0).getKeyNode());
        /*
        List<String> defaultNodes = defaultConfig.getKeys();
        List<String> currentNodes = getKeys();

        for (Map.Entry<String, NodeTuple> entry : defaultNodes.entrySet()) {
            if (!currentNodes.containsKey(entry.getKey()) || overWrite) {
                if (entry.getValue().getValueNode() instanceof SequenceNode
                        || entry.getValue().getValueNode() instanceof ScalarNode) {
                    System.out.println(entry.getKey());
                    System.out.println(entry.getValue().getKeyNode().toString());
                    System.out.println(saveToString());
                    set(entry.getKey(), entry.getValue());

                }
            }
        }

        currentNodes = getAllValueNodes(rootNode);
        for (Map.Entry<String, NodeTuple> entry : defaultNodes.entrySet()) {
            if (currentNodes.containsKey(entry.getKey())) {
                NodeTuple currentNode = currentNodes.get(entry.getKey());
                setComments(currentNode.getKeyNode(), entry.getValue().getKeyNode(), overWriteComments);
                setComments(currentNode.getValueNode(), entry.getValue().getValueNode(), overWriteComments);
            }
        }*/
    }

    public Map<String, NodeTuple> getAllValueNodes() {
        return getAllValueNodes(this, "");
    }

    private Map<String, NodeTuple> getAllValueNodes(AxiomConfigurationSection node, String prefix) {
        Map<String, NodeTuple> nodes = new LinkedHashMap<>();

        if (!prefix.isEmpty())
            prefix += ".";

        for (String key : node.getKeys()) {
            /*
            String newKey = prefix + key;

            Node valueNode = node.getDirectSubNode(key);

            if (valueNode instanceof MappingNode) {
                nodes.putAll(getAllValueNodes(new AxiomConfigurationSection(yaml, (MappingNode) valueNode), newKey));
            } else {
                nodes.put(newKey, new NodeTuple(node.getKeyNode(key), valueNode));
                if (valueNode instanceof ScalarNode) {
                    nodes.put(newKey, ((ScalarNode) valueNode).getValue());
                }
            }*/
        }

        return nodes;
    }
}
