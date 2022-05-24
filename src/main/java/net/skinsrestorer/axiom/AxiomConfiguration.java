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
        dumper.setAllowUnicode(true);
        dumper.setSplitLines(false);
        Constructor constructor = new Constructor(loaderOptions);
        Representer representer = new Representer();

        return new Yaml(constructor, representer, dumper, loaderOptions);
    }

    //
    // Loaders and savers
    //
    public void load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            load(reader);
        }
    }

    public void load(InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
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
        try (BufferedWriter out = Files.newBufferedWriter(path)) {
            out.write(saveToString());
        }
    }

    public String saveToString() {
        StringWriter writer = new StringWriter();
        yaml.serialize(rootNode, writer);
        return writer.toString();
    }
}
