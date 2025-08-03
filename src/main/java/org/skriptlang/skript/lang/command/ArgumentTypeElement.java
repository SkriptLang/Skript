package org.skriptlang.skript.lang.command;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import com.mojang.brigadier.arguments.ArgumentType;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

/**
 * Represents a custom argument type ranging from simple types to more complex structures
 * like {@code integer between 1 and 5}.
 * <p>
 * Implementations of this class are responsible for converting their Skript-specific
 * argument definition into a Brigadier {@link ArgumentType}.
 *
 * @param <T> the type that the Brigadier {@link ArgumentType} will parse the argument into
 *
 * @see Custom
 */
public abstract class ArgumentTypeElement<T> implements SyntaxElement {

	/**
	 * Registry key for the argument types.
	 */
	public static final SyntaxRegistry.Key<SyntaxInfo<? extends ArgumentTypeElement<?>>> REGISTRY_KEY
		= SyntaxRegistry.Key.of("argument type");

	/**
	 * Returns the Brigadier {@link ArgumentType} that corresponds to this argument type element.
	 *
	 * @apiNote If this argument type element represents a custom type not supported
	 * by Paper, and is meant to be used within Paper environment, it is required to provide an
	 * appropriate {@link io.papermc.paper.command.brigadier.argument.CustomArgumentType}
	 * implementation.
	 *
	 * @return a {@link ArgumentType} instance representing this argument type element
	 */
	public abstract ArgumentType<T> toBrigadier();

	@Override
	public @NotNull String getSyntaxTypeName() {
		return "argument type";
	}

	/**
	 * Custom argument type that also implements {@link CustomArgumentType}.
	 * <p>
	 * This class serves as a convenient base for implementing custom argument types
	 * available within Paper environment.
	 *
	 * @param <T> the type that the Brigadier {@link ArgumentType} will parse the argument into
	 * @param <N> the native argument type supported by Paper
	 */
	public abstract static class Custom<T, N> extends ArgumentTypeElement<T> implements CustomArgumentType<T, N> {

		@Override
		public ArgumentType<T> toBrigadier() {
			return this;
		}

	}

	/**
	 * Convenient implementation of ArgumentTypeElement that makes it simple to wrap around
	 * existing argument types.
	 *
	 * @param <T> the type that the Brigadier {@link ArgumentType} will parse the argument into
	 */
	public abstract static class Simple<T> extends ArgumentTypeElement<T> {

		private ArgumentType<T> nativeType;

		protected abstract @Nullable ArgumentType<T> get(Expression<?>[] expressions, int matchedPattern,
				SkriptParser.ParseResult parseResult);

		@Override
		public ArgumentType<T> toBrigadier() {
			return nativeType;
		}

		@Override
		public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
				SkriptParser.ParseResult parseResult) {
			nativeType = get(expressions, matchedPattern, parseResult);
			return nativeType != null;
		}

	}

}
