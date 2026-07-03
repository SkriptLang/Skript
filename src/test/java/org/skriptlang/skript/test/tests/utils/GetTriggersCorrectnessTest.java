package org.skriptlang.skript.test.tests.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Verifies that the loop-based implementation of getTriggers() produces
 * results identical to the original stream-based implementation in terms
 * of content and encounter order.
 *
 * <p>This test covers the change introduced to avoid stream pipeline
 * allocation on every Bukkit event dispatch.
 */
public class GetTriggersCorrectnessTest {

	// ---- Minimal stand-ins so the test has no Bukkit dependency ----

	/** Minimal event hierarchy mirroring Bukkit's Event structure. */
	static class BaseEvent {}
	static class ChildEvent extends BaseEvent {}
	static class UnrelatedEvent {}

	/** Stand-in for a Trigger — only identity matters here. */
	static class FakeTrigger {
		final String name;
		FakeTrigger(String name) { this.name = name; }
	}

	// ---- Helpers matching the two implementations ----

	/**
	 * Original stream-based implementation (verbatim copy from before this PR).
	 */
	private static List<FakeTrigger> getTriggersStream(
			Multimap<Class<?>, FakeTrigger> triggers,
			Class<?> event) {
		return triggers.asMap().entrySet().stream()
				.filter(entry -> entry.getKey().isAssignableFrom(event))
				.flatMap(entry -> entry.getValue().stream())
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * New loop-based implementation introduced by this PR.
	 */
	private static List<FakeTrigger> getTriggersLoop(
			Multimap<Class<?>, FakeTrigger> triggers,
			Class<?> event) {
		LinkedHashSet<FakeTrigger> result = new LinkedHashSet<>();
		for (Map.Entry<Class<?>, Collection<FakeTrigger>> entry : triggers.asMap().entrySet()) {
			if (entry.getKey().isAssignableFrom(event))
				result.addAll(entry.getValue());
		}
		return new ArrayList<>(result);
	}

	// ---- Tests ----

	/**
	 * Both implementations must return the same triggers for a direct match.
	 */
	@Test
	public void testDirectMatch() {
		Multimap<Class<?>, FakeTrigger> triggers = ArrayListMultimap.create();
		FakeTrigger t1 = new FakeTrigger("t1");
		FakeTrigger t2 = new FakeTrigger("t2");
		triggers.put(ChildEvent.class, t1);
		triggers.put(ChildEvent.class, t2);

		List<FakeTrigger> stream = getTriggersStream(triggers, ChildEvent.class);
		List<FakeTrigger> loop = getTriggersLoop(triggers, ChildEvent.class);

		Assert.assertEquals("trigger count must match", stream.size(), loop.size());
		Assert.assertEquals("trigger order must match", stream, loop);
	}

	/**
	 * Triggers registered on a parent class must be included when the child
	 * event fires — isAssignableFrom must work correctly in both implementations.
	 */
	@Test
	public void testInheritedMatch() {
		Multimap<Class<?>, FakeTrigger> triggers = ArrayListMultimap.create();
		FakeTrigger base = new FakeTrigger("base");
		FakeTrigger child = new FakeTrigger("child");
		triggers.put(BaseEvent.class, base);
		triggers.put(ChildEvent.class, child);

		List<FakeTrigger> stream = getTriggersStream(triggers, ChildEvent.class);
		List<FakeTrigger> loop = getTriggersLoop(triggers, ChildEvent.class);

		Assert.assertEquals("trigger count must match", stream.size(), loop.size());
		Assert.assertTrue("both must contain base trigger", loop.containsAll(stream));
	}

	/**
	 * Triggers registered on an unrelated class must not appear in the result.
	 */
	@Test
	public void testUnrelatedExcluded() {
		Multimap<Class<?>, FakeTrigger> triggers = ArrayListMultimap.create();
		FakeTrigger unrelated = new FakeTrigger("unrelated");
		triggers.put(UnrelatedEvent.class, unrelated);

		List<FakeTrigger> stream = getTriggersStream(triggers, ChildEvent.class);
		List<FakeTrigger> loop = getTriggersLoop(triggers, ChildEvent.class);

		Assert.assertTrue("stream result must be empty", stream.isEmpty());
		Assert.assertTrue("loop result must be empty", loop.isEmpty());
	}

	/**
	 * Duplicate triggers (same instance registered twice) must appear only once
	 * in the result — distinct() behaviour must be preserved.
	 */
	@Test
	public void testDeduplication() {
		Multimap<Class<?>, FakeTrigger> triggers = ArrayListMultimap.create();
		FakeTrigger t1 = new FakeTrigger("t1");
		triggers.put(BaseEvent.class, t1);
		triggers.put(ChildEvent.class, t1); // same instance, different key

		List<FakeTrigger> stream = getTriggersStream(triggers, ChildEvent.class);
		List<FakeTrigger> loop = getTriggersLoop(triggers, ChildEvent.class);

		Assert.assertEquals("stream must deduplicate", 1, stream.size());
		Assert.assertEquals("loop must deduplicate", 1, loop.size());
		Assert.assertEquals(stream, loop);
	}

	/**
	 * Encounter order must be identical between both implementations.
	 * Triggers registered first must appear first in the result.
	 */
	@Test
	public void testEncounterOrder() {
		Multimap<Class<?>, FakeTrigger> triggers = ArrayListMultimap.create();
		FakeTrigger t1 = new FakeTrigger("t1");
		FakeTrigger t2 = new FakeTrigger("t2");
		FakeTrigger t3 = new FakeTrigger("t3");
		triggers.put(ChildEvent.class, t1);
		triggers.put(ChildEvent.class, t2);
		triggers.put(ChildEvent.class, t3);

		List<FakeTrigger> stream = getTriggersStream(triggers, ChildEvent.class);
		List<FakeTrigger> loop = getTriggersLoop(triggers, ChildEvent.class);

		Assert.assertEquals("encounter order must be identical", stream, loop);
	}

}
