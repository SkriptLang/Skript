package org.skriptlang.skript.test.tests.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Standalone micro-benchmark comparing the original stream-based getTriggers()
 * implementation against the loop-based replacement introduced in this PR.
 *
 * <p>Run via: {@code java GetTriggersBenchmark}
 *
 * <p>No external dependencies beyond Guava (already on classpath).
 * No JMH required — uses a simple warmup + timed loop approach suitable
 * for a quick before/after comparison.
 *
 * <p>Sample results (JDK 21, typical hardware):
 * <pre>
 * Small map (20 entries, typical server):
 *   stream (original)  avg 359 ns/op
 *   loop   (this PR)   avg 219 ns/op   → 1.64x faster
 *
 * Large map (200 entries, heavy addon scenario):
 *   stream (original)  avg 2305 ns/op
 *   loop   (this PR)   avg 2245 ns/op  → 1.03x (negligible difference)
 * </pre>
 *
 * <p>The gain is most pronounced in the common case (few triggers per event),
 * which is the case on most servers. The improvement comes from eliminating
 * stream pipeline object allocation ({@code ReferencePipeline}, spliterator,
 * collector) on every Bukkit event dispatch.
 */
public class GetTriggersBenchmark {

	// ---- Minimal stand-ins (no Bukkit dependency) ----

	static class BaseEvent {}
	static class ChildEvent extends BaseEvent {}
	static class OtherEvent {}

	static class FakeTrigger {}

	// ---- Implementations under test ----

	static List<FakeTrigger> streamImpl(
			Multimap<Class<?>, FakeTrigger> triggers,
			Class<?> event) {
		return triggers.asMap().entrySet().stream()
				.filter(e -> e.getKey().isAssignableFrom(event))
				.flatMap(e -> e.getValue().stream())
				.distinct()
				.collect(Collectors.toList());
	}

	static List<FakeTrigger> loopImpl(
			Multimap<Class<?>, FakeTrigger> triggers,
			Class<?> event) {
		LinkedHashSet<FakeTrigger> result = new LinkedHashSet<>();
		for (Map.Entry<Class<?>, Collection<FakeTrigger>> entry : triggers.asMap().entrySet()) {
			if (entry.getKey().isAssignableFrom(event))
				result.addAll(entry.getValue());
		}
		return new ArrayList<>(result);
	}

	// ---- Benchmark runner ----

	static long runBenchmark(String name, Multimap<Class<?>, FakeTrigger> triggers,
			Class<?> event, boolean useLoop, int iterations) {
		// Warmup — let JIT compile before measuring
		int warmup = iterations / 10;
		for (int i = 0; i < warmup; i++) {
			if (useLoop)
				loopImpl(triggers, event);
			else
				streamImpl(triggers, event);
		}

		// Timed run — sink prevents JIT from eliminating dead result
		int sink = 0;
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			List<FakeTrigger> result = useLoop
					? loopImpl(triggers, event)
					: streamImpl(triggers, event);
			sink += result.size(); // prevent dead code elimination
		}
		long elapsed = System.nanoTime() - start;
		long nsPerOp = elapsed / iterations;

		System.out.printf("  %-20s %,d iterations  avg %,d ns/op  (total %.1f ms)  [sink=%d]%n",
				name, iterations, nsPerOp, elapsed / 1_000_000.0, sink);

		return nsPerOp;
	}

	public static void main(String[] args) {
		final int ITERATIONS = 2_000_000;

		// Build a multimap representative of a real server:
		// ~20 trigger entries across a small class hierarchy
		Multimap<Class<?>, FakeTrigger> triggers = ArrayListMultimap.create();
		for (int i = 0; i < 5; i++)
			triggers.put(BaseEvent.class, new FakeTrigger());
		for (int i = 0; i < 10; i++)
			triggers.put(ChildEvent.class, new FakeTrigger());
		for (int i = 0; i < 5; i++)
			triggers.put(OtherEvent.class, new FakeTrigger());

		System.out.println("=== getTriggers() benchmark ===");
		System.out.println("Multimap size: " + triggers.size() + " entries across 3 event classes");
		System.out.println("Event under test: ChildEvent (matches BaseEvent + ChildEvent keys)");
		System.out.println();

		System.out.println("--- Small map (20 entries, typical server load) ---");
		long streamNs = runBenchmark("stream (original)", triggers, ChildEvent.class, false, ITERATIONS);
		long loopNs   = runBenchmark("loop   (this PR) ", triggers, ChildEvent.class, true,  ITERATIONS);
		System.out.printf("  Speedup: %.2fx%n%n", (double) streamNs / loopNs);

		// Larger map — stress test with many registered triggers
		Multimap<Class<?>, FakeTrigger> large = ArrayListMultimap.create();
		for (int i = 0; i < 50; i++)
			large.put(BaseEvent.class, new FakeTrigger());
		for (int i = 0; i < 100; i++)
			large.put(ChildEvent.class, new FakeTrigger());
		for (int i = 0; i < 50; i++)
			large.put(OtherEvent.class, new FakeTrigger());

		System.out.println("--- Large map (200 entries, heavy addon scenario) ---");
		long streamNsLarge = runBenchmark("stream (original)", large, ChildEvent.class, false, ITERATIONS);
		long loopNsLarge   = runBenchmark("loop   (this PR) ", large, ChildEvent.class, true,  ITERATIONS);
		System.out.printf("  Speedup: %.2fx%n", (double) streamNsLarge / loopNsLarge);
	}

}
