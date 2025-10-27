package ch.njol.skript.variables;

import ch.njol.skript.classes.Serializer;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.Yggdrasil;
import org.skriptlang.skript.lang.converter.Converter;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

public class StorageMigration {

	static {
		//noinspection removal
		registerMigration(EntityType.class, EntityData.class, "entitytype", new YggdrasilSerializer<>(), existing -> {
			//noinspection removal
			existing.data.setAmount(existing.amount);
			//noinspection removal
			return existing.data;
		});
	}

	private static final Map<Class<?>, Migrator<?, ?>> migrators = new HashMap<>();
	private static final Map<String, Class<?>> toClasses = new HashMap<>();

	public static <From, To> void registerMigration(
			Class<From> from, Class<To> to, String codeName,
			Serializer<From> serializer,
			Converter<From, To> converter
	) {
		Migrator<From, To> migrator = new Migrator<>(from, to, codeName, converter);

		migrators.putIfAbsent(from, migrator);
		toClasses.putIfAbsent(codeName, from);

		Variables.yggdrasil.registerClassResolver(serializer);
	}

	public static Class<?> fromMigration(String from) {
		return toClasses.get(from);
	}

	public static boolean hasMigration(Class<?> from) {
		return migrators.containsKey(from);
	}

	public static <From, To> To migrate(Class<From> from, Object read)
			throws NotSerializableException, StreamCorruptedException {
		Migrator<?, ?> migrator = migrators.get(from);

		if (migrator == null) {
			return null;
		}

		//noinspection unchecked
		return ((Converter<From, To>) migrator.converter).convert((From) read);
	}

	private record Migrator<From, To>(
			Class<From> from, Class<To> to, String codename,
			Converter<From, To> converter) {

	}

}
