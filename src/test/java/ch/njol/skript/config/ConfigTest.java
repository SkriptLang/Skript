package ch.njol.skript.config;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

public class ConfigTest {

	@Test
	public void testNodes() {
		Config reference = getConfig("reference");

		Node node = reference.getNodeAt("a", "g");

		assertNotNull(node);
		assertEquals(node.comments(), List.of("# Comment f", "# Comment f'", ""));
	}

	@Test
	public void testUpdateNodes() {
		Config old = getConfig("test-update");
		Config reference = getConfig("reference");

		boolean updated = old.updateNodes(reference);

		assertTrue("updateNodes did not update any nodes", updated);

		List<Node> newNodes = Config.discoverNodes(reference.getMainNode());
		List<Node> updatedNodes = Config.discoverNodes(old.getMainNode());

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
		try (InputStream resource = getClass().getResourceAsStream("/config/unit/" + name + ".sk")) {
			return new Config(resource, name + ".sk", false, false, ":");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
