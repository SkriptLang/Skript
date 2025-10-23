package org.skriptlang.skript.bukkit.paperutil;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.lang.reflect.Method;

/**
 * Temp enum to mimic 'WeatheringCopperState'.
 */
@Internal
public enum CopperState {

	EXPOSED, OXIDIZED, UNAFFECTED, WEATHERED;

	private static final Class<?> WEATHERING_CLASS;
	private static final Enum<?>[] WEATHERING_VALUES;
	private static final Method WEATHERING_VALUE_METHOD;

	static {
		WEATHERING_CLASS = Skript.getClass("io.papermc.paper.world.WeatheringCopperState");
		if (WEATHERING_CLASS != null) {
			Method valuesMethod = Skript.getMethod(WEATHERING_CLASS, "values");
			WEATHERING_VALUES = Skript.methodInvoke(valuesMethod);
			WEATHERING_VALUE_METHOD = Skript.getMethod(WEATHERING_CLASS, "valueOf", String.class);
		} else {
			WEATHERING_VALUES = null;
			WEATHERING_VALUE_METHOD = null;
		}
	}

	/**
	 * @return {@link CopperState} class or 'WeatheringCopperState' if it exists.
	 */
	public static Class<?> getStateClass() {
		return WEATHERING_CLASS != null ? WEATHERING_CLASS : CopperState.class;
	}

	/**
	 * @return The enum values for {@link CopperState} or 'WeatheringCopperState' if it exists.
	 */
	public static Enum<?>[] getValues() {
		if (WEATHERING_CLASS != null)
			return WEATHERING_VALUES;
		return CopperState.values();
	}

	/**
	 * @param state The {@link CopperState} enum value.
	 * @return {@code state} or the state in 'WeatheringCopperState' if it exists.
	 */
	public static Enum<?> get(CopperState state) {
		if (WEATHERING_CLASS != null)
			return Skript.methodInvoke(WEATHERING_VALUE_METHOD, null, state.name());
		return state;
	}

}
