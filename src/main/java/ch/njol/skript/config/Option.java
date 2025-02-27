package ch.njol.skript.config;

import java.util.Locale;
import java.util.function.Consumer;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.Locale;
import java.util.function.Consumer;

public class Option<T> {

	public final String key;
	private boolean optional = false;

	@Nullable
	private String value = null;
	private final Converter<String, ? extends T> parser;
	private final T defaultValue;
	private T parsedValue;

	@Nullable
	private Consumer<? super T> setter;

	public Option(final String key, final T defaultValue) {
		this.key = "" + key.toLowerCase(Locale.ENGLISH);
		this.defaultValue = defaultValue;
		parsedValue = defaultValue;
		@SuppressWarnings("unchecked")
		final Class<T> c = (Class<T>) defaultValue.getClass();
		if (c == String.class) {
			parser = (Converter<String, T>) s -> (T) s;
		} else {
			final ClassInfo<T> ci = Classes.getExactClassInfo(c);
			final Parser<? extends T> p;
			if (ci == null || (p = ci.getParser()) == null)
				throw new IllegalArgumentException(c.getName());
			this.parser = new Converter<String, T>() {
				@Override
				@Nullable
				public T convert(final String s) {
					final T t = p.parse(s, ParseContext.CONFIG);
					if (t != null)
						return t;
					Skript.error("'" + s + "' is not " + ci.getName().withIndefiniteArticle());
					return null;
				}
			};
		}
	}

	public Option(final String key, final T defaultValue, final Converter<String, ? extends T> parser) {
		this.key = "" + key.toLowerCase(Locale.ENGLISH);
		this.defaultValue = defaultValue;
		parsedValue = defaultValue;
		this.parser = parser;
	}

	public final Option<T> setter(final Consumer<? super T> setter) {
		this.setter = setter;
		return this;
	}

	public final Option<T> optional(final boolean optional) {
		this.optional = optional;
		return this;
	}

	public final void set(final Config config, final String path) {
		final String oldValue = value;
		value = config.getByPath(path + key);
		if (value == null && !optional)
			Skript.error("Required entry '" + path + key + "' is missing in " + config.getFileName() + ". Please make sure that you have the latest version of the config.");
		if ((value == null ^ oldValue == null) || value != null && !value.equals(oldValue)) {
			T parsedValue = value != null ? parser.convert(value) : defaultValue;
			if (parsedValue == null)
				parsedValue = defaultValue;
			this.parsedValue = parsedValue;
			onValueChange();
		}
	}

	protected void onValueChange() {
		if (setter != null)
			setter.accept(parsedValue);
	}

	public final T value() {
		return parsedValue;
	}

	public final T defaultValue() {
		return defaultValue;
	}

	public final boolean isOptional() {
		return optional;
	}

}
