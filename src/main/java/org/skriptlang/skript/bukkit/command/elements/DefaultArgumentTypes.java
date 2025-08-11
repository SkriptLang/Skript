package org.skriptlang.skript.bukkit.command.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.ContextlessEvent;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.command.ArgumentTypeElement;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;

/**
 * Default Argument Types provided by Skript.
 */
public class DefaultArgumentTypes {

	private DefaultArgumentTypes() {
		throw new UnsupportedOperationException();
	}

	/**
	 * String argument type that provides 3 different implementations.
	 * 1. single word
	 * 2. quoted string
	 * 3. greedy string
	 */
	public static final class String extends ArgumentTypeElement.Simple<java.lang.String> {

		public static void load(SkriptAddon addon) {
			addon.syntaxRegistry().register(ArgumentTypeElement.REGISTRY_KEY,
				SyntaxInfo.builder(DefaultArgumentTypes.Integer.class)
					.addPatterns("[single] (word|string)",
						"(quotable|quoted) string",
						"greedy string")
					.supplier(DefaultArgumentTypes.Integer::new)
					.origin(SyntaxOrigin.of(addon))
					.build());
		}

		@Override
		protected @Nullable ArgumentType<java.lang.String> get(Expression<?>[] expressions, int matchedPattern,
				SkriptParser.ParseResult parseResult) {
			return switch (matchedPattern) {
				case 0 -> StringArgumentType.word();
				case 1 -> StringArgumentType.string();
				case 2 -> StringArgumentType.greedyString();
				default -> null;
			};
		}

	}

	/**
	 * Integer argument type.
	 */
	public static final class Integer extends ArgumentTypeElement.Simple<java.lang.Integer> {

		public static void load(SkriptAddon addon) {
			addon.syntaxRegistry().register(ArgumentTypeElement.REGISTRY_KEY,
				SyntaxInfo.builder(DefaultArgumentTypes.Integer.class)
					.addPatterns("integer greater than [equal:or equal to] %integer%",
						"integer less than [equal:or equal to] %integer%",
						"integer between %integer% and %integer%")
					.supplier(DefaultArgumentTypes.Integer::new)
					.origin(SyntaxOrigin.of(addon))
					.build());
		}

		@Override
		protected @NotNull ArgumentType<java.lang.Integer> get(Expression<?>[] expressions, int matchedPattern,
				SkriptParser.ParseResult parseResult) {
			int min = java.lang.Integer.MIN_VALUE;
			int max = java.lang.Integer.MAX_VALUE;

			switch (matchedPattern) {
				case 0 -> {
					min = (int) expressions[0].getSingle(ContextlessEvent.get());
					if (!parseResult.hasTag("equal"))
						min++;
				}
				case 1 -> {
					max = (int) expressions[0].getSingle(ContextlessEvent.get());
					if (!parseResult.hasTag("equal"))
						max--;
				}
				case 2 -> {
					int first = (int) expressions[0].getSingle(ContextlessEvent.get());
					int second = (int) expressions[1].getSingle(ContextlessEvent.get());
					min = Math.min(first, second);
					max = Math.max(first, second);
				}
			}
			return IntegerArgumentType.integer(min, max);
		}

	}

}
