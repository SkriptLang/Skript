package org.skriptlang.skript.test.tests.config;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.ConfigHelper;
import ch.njol.skript.config.Node;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.*;

public class ConfigTest {

	@Test
	public void testIsInvalid() {
		Config valid = getConfig("new-config");
		Config invalid = getConfig("invalid-config");

		assertTrue(valid.getMainNode().isValid());
		assertFalse(invalid.getMainNode().isValid());
	}

	@Test
	public void testUpdateNodes() {
		Config old = getConfig("old-config");
		Config newer = getConfig("new-config");

		boolean updated = old.updateNodes(newer);

		assertTrue("updateNodes did not update any nodes", updated);

		Set<Node> newNodes = ConfigHelper.discoverNodes(newer.getMainNode());
		Set<Node> updatedNodes = ConfigHelper.discoverNodes(old.getMainNode());

		for (Node node : newNodes) {
			assertTrue("Node " + node + " was not updated", updatedNodes.contains(node));
		}

		// maintains old values
		assertEquals("true", old.get("outdated value"));
		assertEquals("true", old.get("a", "outdated value"));
	}

	private Config getConfig(String name) {
		try (InputStream resource = getClass().getResourceAsStream("/" + name + ".sk")) {
			return new Config(resource, name + ".sk", false, false, ":");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
