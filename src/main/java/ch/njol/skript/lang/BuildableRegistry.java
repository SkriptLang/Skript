package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.HashMap;
import java.util.Map;

public class BuildableRegistry {

	private static final Map<Class<?>, BuildableIdentifier> map = new HashMap<>();

	public static <F, T> void registerBuildable(Class<F> from, Class<T> to, Converter<F, T> converter) {
		BuildableIdentifier identifier = new BuildableIdentifier(to, converter);
		if (map.containsKey(from)) {
			throw new SkriptAPIException("A buildable of '" + Classes.toString(from) + "' is already registered to '" +
				Classes.toString(map.get(from).to) + "'.");
		}
		map.put(from, identifier);
	}

	public static <F> boolean isRegistered(Class<F> from) {
		return map.containsKey(from);
	}

	public static <F, T> @Nullable Converter<F, T> getConverter(Class<F> from) {
		if (!isRegistered(from))
			return null;
		BuildableIdentifier identifier = map.get(from);
		//noinspection unchecked
		return (Converter<F, T>) identifier.converter;
	}

	public static <F, T> @Nullable Class<T> getConvertedClass(Class<F> from) {
		if (!isRegistered(from))
			return null;

		BuildableIdentifier identifier = map.get(from);
		//noinspection unchecked
		return (Class<T>) identifier.to;
	}

	public static <F, T> @Nullable T convert(F from) {
		if (from == null)
			return null;

		//noinspection unchecked
		Class<F> fromClass = (Class<F>) from.getClass();
		if (!isRegistered(fromClass))
			return null;
		BuildableIdentifier identifier = map.get(fromClass);
		//noinspection unchecked
		Class<T> toClass = (Class<T>) identifier.to;

		if (toClass.isInstance(from)) {
			//noinspection unchecked
			return (T) from;
		}

		//noinspection unchecked
		Converter<F, T> converter = (Converter<F, T>) identifier.converter;
		return converter.convert(from);
	}

	private record BuildableIdentifier(Class<?> to, Converter<?, ?> converter) {}

}
