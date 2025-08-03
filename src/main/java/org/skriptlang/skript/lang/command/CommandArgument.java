package org.skriptlang.skript.lang.command;

import com.mojang.brigadier.arguments.ArgumentType;
import org.bukkit.command.CommandSender;
import org.skriptlang.skript.brigadier.ArgumentSkriptCommandNode;
import org.skriptlang.skript.brigadier.LiteralSkriptCommandNode;
import org.skriptlang.skript.brigadier.RootSkriptCommandNode;
import org.skriptlang.skript.brigadier.SkriptCommandNode;

/**
 * Represents a parsed command argument (from Skript code).
 */
// TODO optional, possibly plural
public sealed interface CommandArgument {

	/**
	 * Creates an empty builder from given command argument.
	 *
	 * @return empty builder for this command argument
	 */
	SkriptCommandNode.Builder<CommandSender, ?, ?, ?> emptyBuilder();

	/**
	 * Represents a literal.
	 *
	 * @param literal literal
	 */
	record Literal(String literal) implements CommandArgument {
		@Override
		public LiteralSkriptCommandNode.Builder<CommandSender> emptyBuilder() {
			return LiteralSkriptCommandNode.Builder.literal(literal);
		}
	}

	/**
	 * Represents a typed argument parsed from {@link ArgumentTypeElement}.
	 *
	 * @param name name of the argument
	 * @param type argument type
	 * @param <T> argument type
	 */
	record Typed<T>(String name, ArgumentType<T> type) implements CommandArgument {

		public Typed(String name, ArgumentTypeElement<T> typeElement) {
			this(name, typeElement.toBrigadier());
		}

		@Override
		public ArgumentSkriptCommandNode.Builder<CommandSender, T> emptyBuilder() {
			return ArgumentSkriptCommandNode.Builder.argument(name, type);
		}

	}

	/**
	 * Represents a root command node.
	 *
	 * @param label label
	 * @see org.skriptlang.skript.brigadier.RootSkriptCommandNode
	 */
	record Root(String namespace, String label) implements CommandArgument {

		@Override
		public RootSkriptCommandNode.Builder<CommandSender> emptyBuilder() {
			return RootSkriptCommandNode.Builder.root(namespace, label);
		}

	}

}
