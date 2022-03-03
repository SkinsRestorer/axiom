package net.skinsrestorer.axiom;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MergeTest {
    @Test
    public void printNodes() throws IOException {
        AxiomConfiguration config = new AxiomConfiguration();

        String file = null;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("small_test.yml")) {
            assert stream != null;
            file = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n", "", "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert file != null;
        config.load(file);

        System.out.println(config.recursivelyGetAllNodes(config.rootNode).keySet());
    }

    @Test
    public void simpleMergeTest() throws IOException {
        AxiomConfiguration config = new AxiomConfiguration();
        config.load("a: 1\nb: 2\n");
        AxiomConfiguration defaultConfig = new AxiomConfiguration();
        defaultConfig.load("b: 3\nc: 4\n");

        config.mergeDefault(defaultConfig, false, false);
        assertEquals(4, config.getInt("c"));
        assertEquals(2, config.getInt("b"));
    }

    @Test
    public void overwriteMergeTest() throws IOException {
        AxiomConfiguration config = new AxiomConfiguration();
        config.load("a: 1\nb: 2\n");
        AxiomConfiguration defaultConfig = new AxiomConfiguration();
        defaultConfig.load("b: 3\nc: 4\n");

        config.mergeDefault(defaultConfig, false, true);
        System.out.println(config.saveToString());
        assertEquals(4, config.getInt("c"));
        assertEquals(3, config.getInt("b"));
    }

    @Test
    public void overwriteCommentsMergeTest() throws IOException {
        AxiomConfiguration config = new AxiomConfiguration();
        config.load("a: 1\nb: 2\n");
        AxiomConfiguration defaultConfig = new AxiomConfiguration();
        defaultConfig.load("b: 3 # Test\nc: 4 # Test 2\n");

        config.mergeDefault(defaultConfig, true, false);
        System.out.println(config.saveToString());
        assertEquals(4, config.getInt("c"));
        assertEquals(2, config.getInt("b"));
        assertEquals("a: 1\nb: 2 # Test\nc: 4 # Test 2\n", config.saveToString());
    }

    @Test
    public void hardMergeTest() throws IOException {
        AxiomConfiguration config = new AxiomConfiguration();
        String file = null;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("test.yml")) {
            assert stream != null;
            file = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n", "", "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert file != null;
        config.load(file);

        AxiomConfiguration defaultConfig = new AxiomConfiguration();
        defaultConfig.load(file);

        config.set("Debug", null);
        assertNotEquals(defaultConfig.getBoolean("Debug"), config.getBoolean("Debug"));
        assertNotEquals(defaultConfig.saveToString(), config.saveToString());

        config.mergeDefault(defaultConfig, false, false);
        assertEquals(defaultConfig.saveToString(), config.saveToString());
    }

    @Test
    public void commentRootTest() throws IOException {
        AxiomConfiguration config = new AxiomConfiguration();

        String file = null;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("comment_root_test.yml")) {
            assert stream != null;
            file = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n", "", "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert file != null;
        config.load(file);
        assertEquals(file, config.saveToString());

        AxiomConfiguration mergable = new AxiomConfiguration();

        mergable.mergeDefault(config);

        assertEquals(file, mergable.saveToString());
    }
}
