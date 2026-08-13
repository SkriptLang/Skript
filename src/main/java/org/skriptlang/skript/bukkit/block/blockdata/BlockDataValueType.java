package org.skriptlang.skript.bukkit.block.blockdata;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Direction;
import ch.njol.util.coll.iterator.SingleItemIterator;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.misc.elements.expressions.ExprDirection;

import java.util.List;
import java.util.Locale;

/**
 * Helper class for determining the value of a {@link BlockData} tag can be an object other than a {@link String}.
 * @param <Type> The type of value
 */
public interface BlockDataValueType<Type> {

	/**
	 * {@link BlockDataValueType} for {@link BlockData} tags with {@link Integer} values.
	 */
	BlockDataValueType<Integer> INTEGER = new BlockDataValueType<>() {
		@Override
		public Class<Integer> getTypeClass() {
			return Integer.class;
		}

		@Override
		public @Nullable Integer parse(String string) {
			return string.matches("\\d+") ? Integer.parseInt(string) : null;
		}
	};

	/**
	 * {@link BlockDataValueType} for {@link BlockData} tags with {@link Boolean} values.
	 */
	BlockDataValueType<Boolean> BOOLEAN = new BlockDataValueType<>() {
		@Override
		public Class<Boolean> getTypeClass() {
			return Boolean.class;
		}

		@Override
		public @Nullable Boolean parse(String string) {
			return (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false"))
				? Boolean.valueOf(string) : null;
		}

		@Override
		public boolean hasValidityCheck() {
			return false;
		}
	};

	/**
	 * {@link BlockDataValueType} for {@link BlockData} tags with {@link Direction} values.
	 */
	BlockDataValueType<Direction> DIRECTION = new BlockDataValueType<>() {

		@Override
		public Class<Direction> getTypeClass() {
			return Direction.class;
		}

		@Override
		public @Nullable Direction parse(String string) {
			RetainingLogHandler logHandler = SkriptLogger.startRetainingLog();
			Expression<?> expr = null;
			try {
				expr = SkriptParser.parseStatic(string.replace("_", " "), new SingleItemIterator<>(ExprDirection.syntaxInfo), "");
			} finally {
				logHandler.clear();
				logHandler.printErrors();
			}
			if (expr == null || !expr.getReturnType().equals(Direction.class))
				return null;
			return (Direction) expr.getSingle(ContextlessEvent.get());
		}

		@Override
		public boolean requiresConversion() {
			return true;
		}

		@Override
		public String toStringConversion(Direction direction) {
			return Direction.toNearestBlockFace(direction.getDirection()).toString().toLowerCase(Locale.ENGLISH);
		}

	};


	/**
	 * {@link BlockDataValueType} for {@link BlockData} tags with {@link String} values.
	 * This is the default value type and will match against any value.
	 */
	BlockDataValueType<String> STRING = new BlockDataValueType<>() {
		@Override
		public Class<String> getTypeClass() {
			return String.class;
		}

		@Override
		public @Nullable String parse(Object object) {
			if (object instanceof String string)
				return string;
			return object.toString();
		}

		@Override
		public @Nullable String parse(String string) {
			return string;
		}

	};

	/**
	 * List of all {@link BlockDataValueType}s currently supported.
	 */
	List<BlockDataValueType<?>> TYPES = List.of(INTEGER, BOOLEAN, DIRECTION, STRING);

	/**
	 * List of all {@link Class}es that all {@link BlockDataValueType}s handle.
	 */
	Class<?>[] TYPE_CLASSES = TYPES.stream().map(BlockDataValueType::getTypeClass).toArray(Class[]::new);

	/**
	 * @return The {@link Class} {@code this} is bound to.
	 */
	Class<Type> getTypeClass();

	/**
	 * Checks if {@code object} is instance of {@code TYPE} or a {@link String} that can be parsed into {@code TYPE}.
	 * @param object The {@link Object} to parse.
	 * @return The parsed {@code TYPE} if successful, otherwise {@code null}.
	 */
	@SuppressWarnings("unchecked")
	default @Nullable Type parse(Object object) {
		if (getTypeClass().isInstance(object)) {
			return (Type) object;
		} else if (object instanceof String string) {
			return parse(string);
		}
		return null;
	}

	/**
	 * Attempts to parse {@code string} into {@code TYPE}.
	 * @param string The {@link String} to parse.
	 * @return The parsed {@code TYPE} if successful, otherwise {@code null}.
	 */
	@Nullable Type parse(String string);

	/**
	 * Whether {@code this} requires converting {@code Type} for stringification
	 * via {@link #toStringConversion(Object)}
	 * @return {@code true} if conversion required, otherwise {@code false}.
	 */
	default boolean requiresConversion() {
		return false;
	}

	/**
	 * Converts {@code type} for specified stringification.
	 * Primarily for converting {@link Skript} types into {@link Bukkit} types and to be used in {@link BlockData}.
	 * @param type The {@code Type} handled by {@code this}.
	 * @return The converted string representation/
	 */
	default @Nullable String toStringConversion(Type type) {
		return null;
	}

	/**
	 * Whether {@code this} can be checked to ensure a value is valid.
	 * @return {@code true} if can be checked, otherwise {@code false}.
	 */
	default boolean hasValidityCheck() {
		return true;
	}

}
