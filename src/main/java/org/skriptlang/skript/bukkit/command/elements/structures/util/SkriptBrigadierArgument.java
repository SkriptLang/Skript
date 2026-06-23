package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.registrations.Classes;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SkriptBrigadierArgument<T> implements CustomArgumentType.Converted<T, String> {

	private static final Dynamic2CommandExceptionType ERROR_INVALID_INPUT = new Dynamic2CommandExceptionType(
		(input, type) -> new LiteralMessage("'%s' is not a valid %s.".formatted(input, type)));

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
				result = argument.defaultValue().getSingle(ContextlessEvent.get());
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
