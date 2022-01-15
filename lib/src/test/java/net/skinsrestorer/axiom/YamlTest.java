package net.skinsrestorer.axiom;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.nodes.MappingNode;

import java.io.InputStream;

public class YamlTest {
    @Test
    public void loadTest() {
        AxiomConfiguration config = new AxiomConfiguration(null);

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("test.yml")) {
            config.load(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(config.saveToString());
    }
}
