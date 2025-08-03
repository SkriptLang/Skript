package ch.njol.skript.command.brigadier;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.command.ArgumentTypeElement;

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

		static {
			Skript.registerArgumentType(DefaultArgumentTypes.String.class,
				"[single] (word|string)", "[quotable|quoted] string", "greedy string");
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

		static {
			Skript.registerArgumentType(DefaultArgumentTypes.Integer.class,
				"integer [from %-integer% [to %-integer%]]");
		}

		@Override
		protected @NotNull ArgumentType<java.lang.Integer> get(Expression<?>[] expressions, int matchedPattern,
				SkriptParser.ParseResult parseResult) {
			int min = java.lang.Integer.MIN_VALUE;
			int max = java.lang.Integer.MAX_VALUE;
			if (expressions[0] != null)
				min = (int) expressions[0].getSingle(null);
			if (expressions[1] != null)
				max = (int) expressions[1].getSingle(null);
			return IntegerArgumentType.integer(min, max);
		}

	}

}
