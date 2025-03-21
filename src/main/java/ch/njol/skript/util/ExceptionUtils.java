package ch.njol.skript.util;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;

public abstract class ExceptionUtils {
	private ExceptionUtils() {}
	
	private final static String IO_NODE = "io exceptions";
	
	@Nullable
	public static String toString(final IOException e) {
		if (Language.keyExists(IO_NODE + "." + e.getClass().getSimpleName())) {
			return Language.format(IO_NODE + "." + e.getClass().getSimpleName(), e.getLocalizedMessage());
		}
		if (Skript.testing())
			e.printStackTrace();
		return e.getLocalizedMessage();
	}
	
}
