package org.skriptlang.skript.test.tests.config;

import ch.njol.skript.config.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

		Set<Node> newNodes = discoverNodes(newer.getMainNode());
		boolean updated = old.updateNodes(newer);

		assertTrue("updateNodes did not update any nodes", updated);

		Set<Node> oldNodes = discoverNodes(old.getMainNode());

		for (Node node : newNodes) {
			assertTrue("Node " + node + " was not updated", oldNodes.contains(node));
		}
	}

	private Config getConfig(String name) {
		try (InputStream resource = getClass().getResourceAsStream("/" + name + ".sk")) {
			return new Config(resource, name + ".sk", false, false, ":");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Set<Node> discoverNodes(SectionNode node) {
		Set<Node> nodes = new LinkedHashSet<>();

		for (Iterator<Node> iterator = node.fullIterator(); iterator.hasNext(); ) {
			Node child = iterator.next();
			if (child instanceof SectionNode sectionChild) {
				nodes.add(child);
				nodes.addAll(discoverNodes(sectionChild));
			} else if (child instanceof EntryNode || child instanceof VoidNode) {
				nodes.add(child);
			}
		}
		return nodes;
	}

}
