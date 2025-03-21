package ch.njol.skript.config;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import org.skriptlang.skript.lang.converter.Converter;

/**
 * @author Peter Güttinger
 */
public class EnumParser<E extends Enum<E>> implements Converter<String, E> {
	
	private final Class<E> enumType;
	@Nullable
	private final String allowedValues;
	private final String type;
	
	public EnumParser(final Class<E> enumType, final String type) {
		assert enumType != null;
		this.enumType = enumType;
		this.type = type;
		if (enumType.getEnumConstants().length <= 12) {
			final StringBuilder b = new StringBuilder(enumType.getEnumConstants()[0].name());
			for (final E e : enumType.getEnumConstants()) {
				if (b.length() != 0)
					b.append(", ");
				b.append(e.name().toLowerCase(Locale.ENGLISH).replace('_', ' '));
			}
			allowedValues = b.toString();
		} else {
			allowedValues = null;
		}
	}
	
	@Override
	@Nullable
	public E convert(final String s) {
		try {
			return Enum.valueOf(enumType, s.toUpperCase(Locale.ENGLISH).replace(' ', '_'));
		} catch (final IllegalArgumentException e) {
			Skript.error("'" + s + "' is not a valid value for " + type + (allowedValues == null ? "" : ". Allowed values are: " + allowedValues));
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "EnumParser{enum=" + enumType + ",allowedValues=" + allowedValues + ",type=" + type + "}";
	}
}
