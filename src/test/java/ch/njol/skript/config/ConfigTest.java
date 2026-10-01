package ch.njol.skript.config;

import ch.njol.skript.log.RetainingLogHandler;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.Assert.*;

public class ConfigTest {

	private static Config createConfig(String script) {
		try {
			return new Config(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), "test", null,
				true, false, ":");
		} catch (IOException e) { // should not be possible given null file
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testEmptySectionWarning() {
		try (var handler = new RetainingLogHandler().start()) {
			// test with one structure missing a section
			createConfig("""
				on load:
				
				on load:
					stop
				""");
			assertEquals(1, handler.size());
			assertEquals(SectionNode.M_EMPTY_SECTION, handler.getLog().iterator().next().getMessage());
			handler.clear();

			// test with single structure
			createConfig("on load:");
			assertEquals(1, handler.size());
			assertEquals(SectionNode.M_EMPTY_SECTION, handler.getLog().iterator().next().getMessage());
			handler.clear();
		}
	}

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
