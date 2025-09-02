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

		runTests(ref, old, "runtime errors");
	}

	@Test
	public void testMultipleMissingSection() {
		Config ref = getConfig("reference");
		Config old = getConfig("test-multiple-missing-section");

		assertTrue(old.updateNodes(ref));

		runTests(ref, old, "runtime errors");
	}

	@Test
	public void testSectionSectionMissing() {
		Config ref = getConfig("reference");
		Config old = getConfig("test-section-section-missing");

		assertTrue(old.updateNodes(ref));

		runTests(ref, old, "disable hooks");
	}

	@Test
	public void testSectionInSectionMissing() {
		Config ref = getConfig("reference");
		Config old = getConfig("test-section-in-section-missing");

		assertTrue(old.updateNodes(ref));

		runTests(ref, old, "disable hooks");
	}

	private static void runTests(Config ref, Config old, String name) {
		runTests(ref.getMainNode(), old.getMainNode(), name);
	}

	private static void runTests(Node ref, Node old, String name) {
		Node refNode = ref.getNodeAt(name);
		Node oldNode = old.getNodeAt(name);

		assertNotNull(refNode);
		assertNotNull(oldNode);

		runTest(refNode, oldNode);
	}

	private static void runTest(Node ref, Node old) {
		assertEquals(ref.getKey(), old.getKey());
		if (ref instanceof EntryNode refEntry && old instanceof EntryNode oldEntry) {
			assertEquals(refEntry.value(), oldEntry.value());
		}
		assertEquals(ref.comments(), old.comments());
		assertEquals(ref.getIndex(), old.getIndex());

		Iterator<Node> refIterator = ref.iterator();
		Iterator<Node> oldIterator = old.iterator();
		while (refIterator.hasNext() && oldIterator.hasNext()) {
			runTest(refIterator.next(), oldIterator.next());
		}
		assertTrue(!refIterator.hasNext() && !oldIterator.hasNext());
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
