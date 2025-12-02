package ch.njol.skript.config;

import org.junit.Test;

import ch.njol.util.NonNullPair;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class NodeTest {

	@Test
	public void testGetPathSteps() {
		Config newer = getConfig("reference");

		Node node = newer.getNodeAt("a", "b", "c");

		assertNotNull(node);
		assertArrayEquals(new String[] {"a", "b", "c"}, node.getPathSteps());
	}

	@Test
	public void testIsValid() {
		Config valid = getConfig("reference");
		Config invalid = getConfig("test-invalid");

		assertTrue(valid.getMainNode().isValid());
		assertFalse(invalid.getMainNode().isValid());
	}

	@Test
	public void splitLineTest() {
		String[][] data = {
				{"", "", ""},
				{"ab", "ab", ""},
				{"ab#", "ab", "#"},
				{"ab##", "ab#", ""},
				{"ab###", "ab#", "#"},
				{"#ab", "", "#ab"},
				{"ab#cd", "ab", "#cd"},
				{"ab##cd", "ab#cd", ""},
				{"ab###cd", "ab#", "#cd"},
				{"######", "", "######"},
				{"#######", "", "#######"},
				{"#### # ####", "", "#### # ####"},
				{"##### ####", "", "##### ####"},
				{"#### #####", "", "#### #####"},
				{"#########", "", "#########"},
				{"a##b#c##d#e", "a#b", "#c##d#e"},
				{" a ## b # c ## d # e ", " a # b ", "# c ## d # e "},
				{"a b \"#a  ##\" # b \"", "a b \"#a  ##\" ", "# b \""},
		};
		for (String[] d : data) {
			NonNullPair<String, String> p = Node.splitLine(d[0]);
			assertArrayEquals(d[0], new String[] {d[1], d[2]}, new String[] {p.getFirst(), p.getSecond()});
		}

	}

	private Config getConfig(String name) {
		try (InputStream resource = getClass().getResourceAsStream("/config/unit/" + name + ".sk")) {
			assertNotNull(resource);

			return new Config(resource, name + ".sk", false, false, ":");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
