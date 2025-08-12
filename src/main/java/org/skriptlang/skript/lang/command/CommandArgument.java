package org.skriptlang.skript.lang.command;

import com.mojang.brigadier.arguments.ArgumentType;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.brigadier.ArgumentSkriptCommandNode;
import org.skriptlang.skript.brigadier.LiteralSkriptCommandNode;
import org.skriptlang.skript.brigadier.RootSkriptCommandNode;
import org.skriptlang.skript.brigadier.SkriptCommandNode;

import java.util.Collections;
import java.util.List;

/**
 * Represents a parsed command argument (from Skript code).
 * <p>
 * In case of literal command arguments, single argument can represent multiple command
 * nodes, e.g. {@code (req|request)} is a single command argument but must be represented
 * by two literal command nodes, 'req' and 'request'.
 */
// TODO optional, possibly plural
public sealed interface CommandArgument {

	/**
	 * Creates an empty builders from given command argument.
	 * <p>
	 * In case of literal command arguments, single argument can represent multiple command
	 * nodes, e.g. {@code (req|request)} is a single command argument but must be represented
	 * by two literal command nodes, 'req' and 'request'.
	 *
	 * @return empty builders for this command argument
	 */
	List<? extends SkriptCommandNode.Builder<SkriptCommandSender, ?>> emptyBuilders();

	/**
	 * Represents a literal or a group of literals.
	 *
	 * @param literals literals
	 */
	record Literal(@Unmodifiable List<String> literals) implements CommandArgument {

		public Literal(String... literals) {
			this(List.of(literals));
		}

		public Literal {
			literals = Collections.unmodifiableList(literals);
		}

		@Override
		public List<LiteralSkriptCommandNode.Builder<SkriptCommandSender>> emptyBuilders() {
			return literals.stream().map(LiteralSkriptCommandNode.Builder::literal).toList();
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
		public List<ArgumentSkriptCommandNode.Builder<SkriptCommandSender, T>> emptyBuilders() {
			return Collections.singletonList(ArgumentSkriptCommandNode.Builder.argument(name, type));
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
		public List<RootSkriptCommandNode.Builder<SkriptCommandSender>> emptyBuilders() {
			return Collections.singletonList(RootSkriptCommandNode.Builder.root(namespace, label));
		}

	}

}
