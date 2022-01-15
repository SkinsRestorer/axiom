package net.skinsrestorer.axiom;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class AxiomConfiguration {
    private final Yaml yaml;
    private final Path path;
    private Node config;

    {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true);
        DumperOptions dumper = new DumperOptions();
        dumper.setProcessComments(true);
        Constructor constructor = new Constructor(loaderOptions);
        Representer representer = new Representer();
        yaml = new Yaml(constructor, representer, dumper, loaderOptions);
    }

    public AxiomConfiguration(Path path) {
        this.path = path;
    }

    public void load() throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            try (InputStreamReader reader = new InputStreamReader(input)) {
                config = yaml.compose(reader);
            }
        }
    }

    public void load(InputStream input) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(input)) {
            config = yaml.compose(reader);
        }
    }

    public Node getConfig() {
        return config;
    }

    public void save() throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
                yaml.dump(config, writer);
            }
        }
    }

    public String saveToString() {
        StringWriter writer = new StringWriter();
        yaml.serialize(config, writer);
        return writer.toString();
    }

    /*
    public Object get(String path) {
        try {
            return config.node((Object[]) path.split("\\."));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ConfigurationNode get(String path, String defValue) {
        // Save new values if enabled (locale file)
        if (get(path).virtual() && setMissing) {
            logger.info("Saving new config value " + path + " to " + name);
            set(path, defValue);
        }

        return get(path);
    }

    public boolean getBoolean(String path) {
        return (boolean) get(path);
    }

    public boolean getBoolean(String path, Boolean defValue) {
        return get(path).getBoolean(defValue);
    }

    public int getInt(String path) {
        return get(path).getInt();
    }

    public int getInt(String path, Integer defValue) {
        return get(path).getInt(defValue);
    }

    private String getString(String path) {
        return get(path).getString();
    }

    public String getString(String path, String defValue) {
        return get(path, defValue).getString(defValue);
    }

    public List<String> getStringList(String path) {
        try {
            return get(path).getList(String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public void set(String path, Object value) {
        try {
            ConfigurationNode node = config.node((Object[]) path.split("\\."));
            if (value instanceof List) {
                node.setList(String.class, (List<String>) value);
            } else {
                node.set(value);
            }

            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
