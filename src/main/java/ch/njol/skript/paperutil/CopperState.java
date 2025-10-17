package ch.njol.skript.paperutil;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.util.ReflectUtils;

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
		WEATHERING_CLASS = ReflectUtils.getClass("io.papermc.paper.world.WeatheringCopperState");
		if (WEATHERING_CLASS != null) {
			Method valuesMethod = ReflectUtils.getMethod(WEATHERING_CLASS, "values");
			WEATHERING_VALUES = ReflectUtils.methodInvoke(valuesMethod);
			WEATHERING_VALUE_METHOD = ReflectUtils.getMethod(WEATHERING_CLASS, "valueOf", String.class);
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

	public static Enum<?>[] getValues() {
		if (WEATHERING_CLASS != null)
			return WEATHERING_VALUES;
		return CopperState.values();
	}

	public static Enum<?> get(CopperState state) {
		if (WEATHERING_CLASS != null)
			return ReflectUtils.methodInvoke(WEATHERING_VALUE_METHOD, null, state.name());
		return state;
	}

}
