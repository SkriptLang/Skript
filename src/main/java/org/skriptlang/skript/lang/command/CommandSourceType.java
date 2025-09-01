package org.skriptlang.skript.lang.command;

import com.mojang.brigadier.context.CommandContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a type of command source, e.g. console.
 *
 * @param type class representing the command source
 * @param names names of the command source, this is used by the {@code executable by} entry
 * @param checkFunction function to check if the source type can execute command in given context
 */
public record CommandSourceType(Class<? extends SkriptCommandSender> type,
			@Unmodifiable Set<String> names,
			BiFunction<@Nullable CommandContext<SkriptCommandSender>, SkriptCommandSender, Boolean> checkFunction) {

	public CommandSourceType {
		names = Set.copyOf(names);
	}

	/**
	 * Simple command source type that just checks if the
	 * sender is instance of the CommandSourceType type.
	 *
	 * @param type lass representing the command source
	 * @param names names of the command source, this is used by the {@code executable by} entry
	 * @return command source type instance
	 */
	public static CommandSourceType typed(Class<? extends SkriptCommandSender> type, String... names) {
		return new CommandSourceType(type, Set.of(names), ((context, sender) -> type.isInstance(sender)));
	}

	/**
	 * Simple command source type that applies check function on instance of specific command sender implementation.
	 *
	 * @param sourceType expected skript command sender type
	 * @param checkFunction check function
	 * @param names names of the command source, this is used by the {@code executable by} entry
	 * @return command source type instance
	 * @param <T> expected skript command sender type
	 */
	public static <T extends SkriptCommandSender> CommandSourceType simple(Class<T> sourceType,
			Function<T, Boolean> checkFunction, String... names) {
		return new CommandSourceType(SkriptCommandSender.class, Set.of(names),
			(ctx, sender) -> {
				if (!sourceType.isInstance(sender)) return false;
				return checkFunction.apply(sourceType.cast(sender));
			}
		);
	}

	/**
	 * Checks whether given sender is instance of this source type.
	 *
	 * @param sender sender to check
	 * @return if the sender is of this type
	 * @param <S> command sender type
	 */
	@SuppressWarnings("unchecked")
	public <S extends SkriptCommandSender> boolean check(@Nullable CommandContext<S> context, S sender) {
		return checkFunction.apply((CommandContext<SkriptCommandSender>) context, sender);
	}

}
