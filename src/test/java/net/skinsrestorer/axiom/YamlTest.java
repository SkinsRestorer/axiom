package net.skinsrestorer.axiom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class YamlTest {
    @Test
    @DisplayName("Test if a small config can be loaded and dumped correctly")
    public void loadSmallTest() throws IOException {
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
        assertEquals(file, config.saveToString());
    }

    @Test
    @DisplayName("Test if a SkinsRestorer config can be loaded and dumped correctly")
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
        config.load(file);
        assertEquals(file, config.saveToString());
    }

    @Test
    @DisplayName("Test setting a value in a empty config")
    public void setEmptyTest() {
        AxiomConfiguration config = new AxiomConfiguration();

        assertNull(config.getBoolean("a.b.c"));
        config.set("a.b.c", true);
        assertEquals(true, config.getBoolean("a.b.c"));
    }

    @Test
    @DisplayName("Test setting a value")
    public void setTest() throws IOException {
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
        assertEquals(true, config.getBoolean("a.b.c"));
        config.set("a.b.c", false);
        assertEquals(false, config.getBoolean("a.b.c"));
    }

    @Test
    @DisplayName("Test setting a new value")
    public void setNewTest() throws IOException {
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
        assertNull(config.getBoolean("a.b.z"));
        config.set("a.b.z", true);
        assertEquals(true, config.getBoolean("a.b.z"));
    }

    @Test
    @DisplayName("Test setting a null value")
    public void setNullTest() throws IOException {
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

        assertEquals(true, config.getBoolean("a.b.c"));
        config.set("a.b.c", null);
        assertNull(config.getBoolean("a.b.c"));
    }

    @Test
    @DisplayName("Test setting a new value")
    public void setListTest() throws IOException {
        AxiomConfiguration config = new AxiomConfiguration();

        String file = null;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("small_test.yml")) {
            assert stream != null;
            file = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n", "", "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String file2 = null;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("list_test.yml")) {
            assert stream != null;
            file2 = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n", "", "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert file != null;
        config.load(file);

        List<String> list = new ArrayList<>();
        list.add("true");
        list.add("false");
        list.add("true");

        assertNull(config.getStringList("a.b.l"));
        config.set("a.b.l", list);
        assertEquals(3, config.getStringList("a.b.l").size());
        assertEquals(list, config.getStringList("a.b.l"));

        config.set("a.b.d", null);
        config.set("a.b.e", null);
        config.set("a.b.c", null);
        assertEquals(file2, config.saveToString());
    }

    @Test
    public void getKeysTest() throws IOException {
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

        assertEquals(3, config.getSection("a.b").getKeys().size());
    }
}
