package ch.njol.skript.timings;

import ch.njol.skript.Skript;

/**
 * Profiler instance for the ProfilerAPI
 * Equivalent to a Timing
 */
public class Profiler {
	private final String name;
	private long startTime = 0;
	private long totalTime = 0;
	private boolean running = false;

	public Profiler(String name) {
		this.name = name;
	}

	public void start() {
		if (running) return; // prevent double-starts
		startTime = System.nanoTime();
		running = true;
	}

	public void stop() {
		if (!running) return; // prevent stop without start
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		totalTime += duration;
		running = false;

		Skript.debug("[Profiler] " + name + " took " + (duration / 1_000_000.0) + " ms");
	}

	public long getTimeNanos() {
		return totalTime;
	}

	public double getTime() {
		return totalTime / 1_000_000.0;
	}

	public String getName() {
		return name;
	}

}
