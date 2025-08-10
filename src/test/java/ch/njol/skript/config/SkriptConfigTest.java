package ch.njol.skript.config;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.junit.Assert.*;

public class SkriptConfigTest {

	@Test
	public void testSingleMissing() {
		Config ref = getConfig("reference");
		Config old = getConfig("test-single-missing");

		assertTrue(old.updateNodes(ref));

		runTests(ref, old, "enable timings");
	}

	//	enable effect commands
	@Test
	public void testMultipleMissing() {
		Config ref = getConfig("reference");
		Config old = getConfig("test-multiple-missing");

		assertTrue(old.updateNodes(ref));

		runTests(ref, old, "enable timings");
		runTests(ref, old, "enable effect commands");
	}

	@Test
	public void testSectionMissing() {
		Config ref = getConfig("reference");
		Config old = getConfig("test-section-missing");

		assertTrue(old.updateNodes(ref));

		SectionNode refNode = (SectionNode) ref.getNodeAt("runtime errors");
		SectionNode oldNode = (SectionNode) old.getNodeAt("runtime errors");

		assertNotNull(refNode);
		assertNotNull(oldNode);

		assertEquals(refNode.getKey(), oldNode.getKey());
		Iterator<Node> refIterator = refNode.iterator();
		Iterator<Node> oldIterator = oldNode.iterator();

		while (refIterator.hasNext() && oldIterator.hasNext()) {
			assertEquals(refIterator.next(), oldIterator.next());
		}
		assertTrue(!refIterator.hasNext() && !oldIterator.hasNext());

		assertEquals(refNode.comments(), oldNode.comments());
		assertEquals(refNode.getIndex(), oldNode.getIndex());
	}

	@Test
	public void testMultipleMissingSection() {
		Config ref = getConfig("reference");
		Config old = getConfig("test-multiple-missing-section");

		assertTrue(old.updateNodes(ref));

		Node refRuntimeErrors = ref.getNodeAt("runtime errors");
		Node oldRuntimeErrors = old.getNodeAt("runtime errors");

		assertNotNull(refRuntimeErrors);
		assertNotNull(oldRuntimeErrors);

		runTests(refRuntimeErrors, oldRuntimeErrors, "frame duration");
		runTests(refRuntimeErrors, oldRuntimeErrors, "errors from one line per frame");
	}

	private static void runTests(Config ref, Config old, String name) {
		runTests(ref.getMainNode(), old.getMainNode(), name);
	}

	private static void runTests(Node ref, Node old, String name) {
		EntryNode refNode = (EntryNode) ref.getNodeAt(name);
		EntryNode oldNode = (EntryNode) old.getNodeAt(name);

		assertNotNull(refNode);
		assertNotNull(oldNode);

		assertEquals(refNode.getKey(), oldNode.getKey());
		assertEquals(refNode.value(), oldNode.value());
		assertEquals(refNode.comments(), oldNode.comments());
		assertEquals(refNode.getIndex(), oldNode.getIndex());
	}

	private Config getConfig(String name) {
		try (InputStream resource = getClass().getResourceAsStream("/config/skript/" + name + ".sk")) {
			assertNotNull(resource);

			return new Config(resource, name + ".sk", false, false, ":");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}


}
