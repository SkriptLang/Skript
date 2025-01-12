package ch.njol.skript.config;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.*;

public class ConfigTest {

	@Test
	public void testUpdateNodes() {
		Config old = getConfig("old-config");
		Config newer = getConfig("new-config");

		boolean updated = old.updateNodes(newer);

		assertTrue("updateNodes did not update any nodes", updated);

		Set<Node> newNodes = Config.discoverNodes(newer.getMainNode());
		Set<Node> updatedNodes = Config.discoverNodes(old.getMainNode());

		for (Node node : newNodes) {
			assertTrue("Node " + node + " was not updated", updatedNodes.contains(node));

			if (node instanceof EntryNode entry) {
				assertEquals(entry.getValue(), old.get(entry.getPathSteps()));
			}
		}

		// keeps removed/user-added nodes
		assertEquals("true", old.getValue("outdated value"));
		assertEquals("true", old.get("a", "outdated value"));

		// adds new nodes
		assertEquals("h.c", old.getValue("true"));
		assertEquals("k", old.getValue("true"));

		// keeps values of nodes
		assertEquals("false", old.getValue("j"));

		// doesnt duplicate nested
		SectionNode node = (SectionNode) old.get("h");
		assertNotNull(node);
		assertEquals(2, node.size());
	}

	private Config getConfig(String name) {
		try (InputStream resource = getClass().getResourceAsStream("/" + name + ".sk")) {
			return new Config(resource, name + ".sk", false, false, ":");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
