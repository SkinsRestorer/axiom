package net.skinsrestorer.axiom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YamlTest {
    @Test
    public void loadSmallTest() {
        AxiomConfiguration config = new AxiomConfiguration();

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("small_test.yml")) {
            config.load(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(config.getBoolean("a.b.c"));
        System.out.println(config.getStringList("a.b.d"));
    }

    @Test
    public void loadTest() throws IOException {
        AxiomConfiguration config = new AxiomConfiguration();

        String file = null;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("test.yml")) {
            assert stream != null;
            file = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n", "", "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert file != null;
        config.load(new StringReader(file));
        assertEquals(file, config.saveToString());
    }
}
