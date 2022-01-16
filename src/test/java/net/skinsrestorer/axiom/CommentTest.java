package net.skinsrestorer.axiom;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CommentTest {
    @Test
    public void setNodeWithComment() throws IOException {
        String data = "a: 1 # comment\n";

        AxiomConfiguration config = new AxiomConfiguration();

        config.load(data);

        assertEquals(1, config.getInt("a"));
        config.set("a", 2);
        assertEquals(2, config.getInt("a"));
        assertEquals("a: 2 # comment\n", config.saveToString());
    }

    @Test
    public void complexCommentTest() throws IOException {
        AxiomConfiguration config = new AxiomConfiguration();

        String data = "d: 1\n";

        String file = null;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("comment_test.yml")) {
            assert stream != null;
            file = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n", "", "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert file != null;
        config.load(file);
        assertEquals(file, config.saveToString());

        AxiomConfiguration defaultConfig = new AxiomConfiguration();
        defaultConfig.load(data);
        config.mergeDefault(defaultConfig);

        assertNotEquals(file, config.saveToString());
        assertEquals(1, config.getInt("d"));
        assertEquals(true, config.getBoolean("a.b"));
    }
}
