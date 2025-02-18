package ch.njol.skript.config;

import java.lang.reflect.Field;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class OptionSection {
	
	public final String key;
	
	public OptionSection(final String key) {
		this.key = key;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public final <T> T get(String key) {
		if (this.getClass() == OptionSection.class)
			return null;
		key = "" + key.toLowerCase(Locale.ENGLISH);
		for (final Field f : this.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			if (Option.class.isAssignableFrom(f.getType())) {
				try {
					final Option<?> o = (Option<?>) f.get(this);
					if (o.key.equals(key))
						return (T) o.value();
				} catch (final IllegalArgumentException e) {
					assert false;
				} catch (final IllegalAccessException e) {
					assert false;
				}
			}
		}
		return null;
	}
	
}
