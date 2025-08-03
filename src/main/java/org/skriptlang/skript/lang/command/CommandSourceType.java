package org.skriptlang.skript.lang.command;

import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * Represents a type of command source, e.g. console.
 *
 * @param type class representing the command source
 * @param names names of the command source, this is used by the {@code executable by} entry
 * @param checkFunction function to check if the source type can execute command in given context
 */
public record CommandSourceType(Class<? extends CommandSender> type,
			@Unmodifiable Set<String> names,
			BiFunction<@Nullable CommandContext<CommandSender>, CommandSender, Boolean> checkFunction) {

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
	public static CommandSourceType typed(Class<? extends CommandSender> type, String... names) {
		return new CommandSourceType(type, Set.of(names), ((context, sender) -> type.isInstance(sender)));
	}

	/**
	 * Checks whether given sender is instance of this source type.
	 *
	 * @param sender sender to check
	 * @return if the sender is of this type
	 * @param <S> command sender type
	 */
	@SuppressWarnings("unchecked")
	public <S extends CommandSender> boolean check(@Nullable CommandContext<S> context, S sender) {
		return checkFunction.apply((CommandContext<CommandSender>) context, sender);
	}

}
