package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.lang.ParseContext;
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
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.entity.Player;
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
class SkriptBrigadierArgument<T> implements CustomArgumentType.Converted<T, String> {

	/**
	 * Pre-defined mappings of types that are acceptable to map to other native argument types.
	 */
	public static final Map<Class<?>, Function<ArgumentData<?>, ArgumentType<?>>> ARGUMENT_TYPE_MAPPINGS = Map.of(
		Boolean.class, ignored -> BoolArgumentType.bool(),
		Long.class, data -> {
			if (data.max() == null) {
				if (data.min() == null) {
					return LongArgumentType.longArg();
				}
				return LongArgumentType.longArg((long) data.min());
			}
			return LongArgumentType.longArg((long) data.min(), (long) data.max());
		},
		Number.class, data -> {
			if (data.max() == null) {
				if (data.min() == null) {
					return DoubleArgumentType.doubleArg();
				}
				return DoubleArgumentType.doubleArg((double) data.min());
			}
			return DoubleArgumentType.doubleArg((double) data.min(), (double) data.max());
		},
		Player.class, ignored -> ArgumentTypes.player()
	);

	private static final Dynamic2CommandExceptionType ERROR_INVALID_INPUT = new Dynamic2CommandExceptionType(
		(input, type) -> new LiteralMessage("'%s' is not a valid %s.".formatted(input, type)));

	/**
	 * Placeholder result to be used for indicating that the default value of an argument should be resolved.
	 * Resolution of default values is only possible during general execution (after this argument is evaluated),
	 *  as they may depend on the context of the command execution.
	 */
	static final Object DEFAULT_VALUE_PLACEHOLDER = new Object();

	private final ArgumentData<T> argument;
	private final StringArgumentType nativeType;

	public SkriptBrigadierArgument(@NotNull ArgumentData<T> argument, @NotNull StringArgumentType nativeType) {
		this.argument = argument;
		this.nativeType = nativeType;
	}

	public ArgumentData<T> getArgument() {
		return argument;
	}

	@Override
	public @NotNull T convert(@NotNull String input) throws CommandSyntaxException {
		assert argument.type().getParser() != null;
		input = input.replace('_', ' ');
		T result = argument.type().getParser().parse(input, ParseContext.COMMAND);
		if (result == null) {
			if (argument.defaultValue() != null) { // attempt default value
				//noinspection unchecked
				result = (T) DEFAULT_VALUE_PLACEHOLDER;
			}
			if (result == null) {
				throw ERROR_INVALID_INPUT.create(input, argument.type().getName().getSingular());
			}
		}
		return result;
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
