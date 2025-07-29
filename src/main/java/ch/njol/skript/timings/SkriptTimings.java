package ch.njol.skript.timings;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;

/**
 * Static utils for Skript timings.
 */
@SuppressWarnings("removal")
public class SkriptTimings {

	private static volatile boolean enabled;
	@SuppressWarnings("null")
	private static Skript skript; // Initialized on Skript load, before any timings would be used anyway

	@Nullable
	public static Object start(String name) {
		if (!enabled()) // Timings disabled :(
			return null;
		Timing timing = Timings.of(skript, name);
		timing.startTimingIfSync(); // No warning spam in async code
		assert timing != null;
		return timing;
	}

	public static void stop(@Nullable Object timing) {
		if (timing == null) // Timings disabled...
			return;
		((Timing) timing).stopTimingIfSync();
	}

	public static boolean enabled() {
		// First check if we can run timings (enabled in settings + running Paper)
		// After that (we know that class exists), check if server has timings running
		return enabled && Timings.isTimingsEnabled();
	}

	public static void setEnabled(boolean flag) {
		enabled = flag;
	}

	public static void setSkript(Skript plugin) {
		skript = plugin;
	}

}
