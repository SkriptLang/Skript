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
		}

		// keeps removed/user-added nodes
		assertEquals("true", old.get(new String[] {"outdated value"}));
		assertEquals("true", old.get("a", "outdated value"));

		// adds new nodes
		assertEquals("true", old.get("h", "c"));
		assertEquals("true", old.get(new String[] {"l"}));

		// keeps values of nodes
		assertEquals("false", old.get(new String[] {"j"}));
		assertEquals("false", old.get(new String[] {"k"}));

		// doesnt duplicate nested
		SectionNode node = (SectionNode) old.get("h");
		assertNotNull(node);

		int size = 0;
		for (Node ignored : node) { // count non-void nodes
			size++;
		}

		assertEquals(2, size);
	}

	private Config getConfig(String name) {
		try (InputStream resource = getClass().getResourceAsStream("/" + name + ".sk")) {
			return new Config(resource, name + ".sk", false, false, ":");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
