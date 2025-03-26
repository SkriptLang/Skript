package ch.njol.skript.classes;

import ch.njol.skript.command.Commands;
import ch.njol.skript.util.Utils;
import org.jetbrains.annotations.Nullable;

/**
 * <h2>WARNING! This class has been removed in this update.</h2>
 * This class stub has been left behind to prevent loading errors from outdated addons,
 * but its functionality has been largely removed.
 *
 * @deprecated Use {@link org.skriptlang.skript.lang.converter.Converter}
 */
@Deprecated(forRemoval = true)
public interface Converter<F, T> extends org.skriptlang.skript.lang.converter.Converter<F, T> {

	// Interfaces don't have a <clinit> so we trigger the warning notice with this
	int $_WARNING = Utils.loadedRemovedClassWarning(Converter.class);

	@Deprecated(forRemoval = true)
	int NO_LEFT_CHAINING = org.skriptlang.skript.lang.converter.Converter.NO_LEFT_CHAINING;
	@Deprecated(forRemoval = true)
	int NO_RIGHT_CHAINING = org.skriptlang.skript.lang.converter.Converter.NO_RIGHT_CHAINING;
	@Deprecated(forRemoval = true)
	int NO_CHAINING = NO_LEFT_CHAINING | NO_RIGHT_CHAINING;
	@Deprecated(forRemoval = true)
	int NO_COMMAND_ARGUMENTS = Commands.CONVERTER_NO_COMMAND_ARGUMENTS;

	@Deprecated(forRemoval = true)
	@Nullable T convert(F f);

	@Deprecated(forRemoval = true)
	final class ConverterUtils {

		@Deprecated(forRemoval = true)
		public static <F, T> Converter<?, T> createInstanceofConverter(Class<F> from, Converter<F, T> conv) {
			throw new UnsupportedOperationException();
		}

		@Deprecated(forRemoval = true)
		public static <F, T> Converter<F, T> createInstanceofConverter(Converter<F, ?> conv, Class<T> to) {
			throw new UnsupportedOperationException();
		}

		@Deprecated(forRemoval = true)
		public static <F, T> Converter<?, T>
		createDoubleInstanceofConverter(Class<F> from, Converter<F, ?> conv, Class<T> to) {
			throw new UnsupportedOperationException();
		}

	}

}
