/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.timings;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;

/**
 * Static utils for Skript timings.
 */
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
