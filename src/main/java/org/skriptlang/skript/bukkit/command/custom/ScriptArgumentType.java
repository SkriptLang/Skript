package org.skriptlang.skript.bukkit.command.custom;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.registrations.Classes;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A custom argument for wrapping {@link ch.njol.skript.classes.ClassInfo}s with a {@link ch.njol.skript.classes.Parser}.
 * This is natively a {@link StringArgumentType} with conversion occurring during execution.
 * @param <T> The real type of the argument.
 */
@ApiStatus.Internal
public class ScriptArgumentType<T> implements CustomArgumentType.Converted<Object, String> {

	/**
	 * Data about an available native argument type.
	 * A native argument type is one that is recognized by the client, providing enhanced validation features.
	 * @param supportsPlural Whether this argument type supports returning multiple values.
	 * @param supportsRange Whether this argument type supports minimum or maximum values.
	 * @param mapper A function for obtaining the native argument type from an argument data.
	 */
	public record NativeArgumentData(
		boolean supportsPlural,
		boolean supportsRange,
		Function<ArgumentData<?>, ArgumentType<?>> mapper
	) {

		public NativeArgumentData {
			if (!supportsPlural) {
				final var inputMapper = mapper;
				mapper = data -> data.isSingle() ? inputMapper.apply(data) : null;
			}
		}

		public NativeArgumentData(Function<ArgumentData<?>, ArgumentType<?>> mapper) {
			this(false, false, mapper);
		}

	}

	/**
	 * Pre-defined mappings of types that are acceptable to map to other native argument types.
	 */
	public static final Map<Class<?>, NativeArgumentData> ARGUMENT_TYPE_MAPPINGS = Map.of(
		Boolean.class, new NativeArgumentData(ignored -> BoolArgumentType.bool()),
		Long.class, new NativeArgumentData(false, true, data -> {
			Long min = (Long) data.min();
			Long max = (Long) data.max();
			if (min == null) {
				if (max == null) {
					return LongArgumentType.longArg();
				}
				return LongArgumentType.longArg(Long.MIN_VALUE, max);
			} else if (max == null) {
				return LongArgumentType.longArg(min);
			}
			return LongArgumentType.longArg(min, max);
		}),
		Number.class, new NativeArgumentData(false, true, data -> {
			Number min = (Number) data.min();
			Number max = (Number) data.max();
			if (min == null) {
				if (max == null) {
					return DoubleArgumentType.doubleArg();
				}
				return DoubleArgumentType.doubleArg(Double.MIN_VALUE, max.doubleValue());
			} else if (max == null) {
				return DoubleArgumentType.doubleArg(min.doubleValue());
			}
			return DoubleArgumentType.doubleArg(min.doubleValue(), max.doubleValue());
		}),
		Player.class, new NativeArgumentData(true, false, data ->
			data.isSingle() ? ArgumentTypes.player() : ArgumentTypes.players()),
		Entity.class, new NativeArgumentData(true, false, data ->
			data.isSingle() ? ArgumentTypes.entity() : ArgumentTypes.entities())
	);

	private static final DynamicCommandExceptionType ERROR_PARSER_ERROR = new DynamicCommandExceptionType(
		input -> new LiteralMessage((String) input));

	private static final Dynamic2CommandExceptionType ERROR_INVALID_INPUT = new Dynamic2CommandExceptionType(
		(input, type) -> new LiteralMessage("'%s' is not a valid %s.".formatted(input, type)));

	private final ArgumentData<T> argument;
	private final StringArgumentType nativeType;

	public ScriptArgumentType(@NotNull ArgumentData<T> argument, @NotNull StringArgumentType nativeType) {
		this.argument = argument;
		this.nativeType = nativeType;
	}

	@Override
	public @NotNull Object convert(@NotNull String input) throws CommandSyntaxException {
		assert argument.type().getParser() != null;
		input = input.replace('_', ' ');

		Literal<T> result;
		try (ParseLogHandler logHandler = new ParseLogHandler().start()) {
			//noinspection unchecked
			result = (Literal<T>) new SkriptParser(input, SkriptParser.PARSE_LITERALS, ParseContext.COMMAND)
				.parseExpression(argument.type().getC());
			if (result != null && argument.isSingle() && !result.canBeSingle()) { // provided many values but expected one
				result = null;
				Skript.error("Expected one " + argument.type().getName().getSingular() + " but got many.");
			}
			if (result == null) {
				if (logHandler.hasError()) {
					throw ERROR_PARSER_ERROR.create(logHandler.getError());
				} else {
					throw ERROR_INVALID_INPUT.create(input, argument.type().getName().getSingular());
				}
			}
		}

		return argument.isSingle() ? result.getSingle() : result.getArray();
	}

	@Override
	public @NotNull ArgumentType<String> getNativeType() {
		return nativeType;
	}

	@Override
	public @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext context, @NotNull SuggestionsBuilder builder) {
		Supplier<Iterator<T>> supplier = argument.type().getSupplier();
		if (supplier == null) {
			return Suggestions.empty();
		}

		// treat <foo b> as a valid match for <foo_bar>
		// treat <"foo b> as a valid match for <foo_bar>
		supplier.get().forEachRemaining(value -> {
			if (value == null) {
				return;
			}
			String name = Classes.toString(value).toLowerCase(Locale.ENGLISH)
				.replace(' ', '_');
			String remaining = builder.getRemainingLowerCase();
			if (!remaining.isEmpty() && remaining.charAt(0) == '"') {
				remaining = remaining.substring(1);
			}
			if (remaining.contains(" ")) {
				remaining = remaining.replace(' ', '_');
			}
			if (name.startsWith(remaining)) {
				builder.suggest(name);
			}
		});

		return builder.buildFuture();
	}

}
