package ch.njol.skript.timings;

import ch.njol.skript.Skript;
import ch.njol.skript.events.bukkit.ProfileCompletedEvent;
import ch.njol.skript.timings.Profiler;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

/**
 * Timings alternative for post-timings removal
 */
public class ProfilerAPI {

	private static volatile boolean enabled = true;
	public static final ThreadLocal<Boolean> isFiringProfilerEvent = ThreadLocal.withInitial(() -> false);

	@Nullable
	public static Profiler start(String name) {
		if (!enabled()) // Timings disabled :(
			return null;
		Profiler profiler = new Profiler(name);
		profiler.start();
		return profiler;
	}

	public static void stop(@Nullable Profiler profiler) {
		if (profiler == null) // Timings disabled...
			return;
		profiler.stop();

		if (isFiringProfilerEvent.get()) return;
		isFiringProfilerEvent.set(true);
		try {
			Bukkit.getPluginManager().callEvent(new ProfileCompletedEvent(profiler.getName(), profiler.getTime()));
		} finally {
			isFiringProfilerEvent.set(false);
		}
	}

	public static boolean enabled() {
		return enabled;
	}

	public static void setEnabled(boolean flag) {
		enabled = flag;
	}
}
