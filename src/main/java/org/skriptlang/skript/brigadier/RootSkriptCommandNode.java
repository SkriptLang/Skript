package org.skriptlang.skript.brigadier;

import ch.njol.skript.command.CommandUsage;
import ch.njol.skript.lang.VariableString;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.command.CommandCooldown;
import org.skriptlang.skript.lang.command.CommandSourceType;
import org.skriptlang.skript.lang.command.SkriptCommandSender;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Represents a root node for a Skript command.
 * <p>
 * This is different form {@link com.mojang.brigadier.tree.RootCommandNode}, which is used as a root node
 * for a CommandDispatcher.
 * <p>
 * This class just expands on {@link LiteralSkriptCommandNode} with additional properties of a Skript command.
 *
 * @param <S> command source type
 */
public class RootSkriptCommandNode<S extends SkriptCommandSender> extends LiteralSkriptCommandNode<S> {

	private final String namespace;
	private final @Nullable String description;
	private final @Nullable CommandUsage usage;
	private final @Unmodifiable Set<String> aliases = new LinkedHashSet<>();

	protected RootSkriptCommandNode(String namespace, @Nullable String description, @Nullable CommandUsage usage,
			@Nullable Collection<String> aliases, String literal, @Nullable Command<S> command,
			@Nullable Predicate<S> requirement, @Nullable CommandNode<S> redirect,
			@Nullable RedirectModifier<S> modifier, boolean forks, @Nullable String permission,
			@Nullable VariableString permissionMessage, @Nullable Collection<CommandSourceType> possibleSources,
			@Nullable CommandCooldown cooldown) {
		super(literal, command, requirement, redirect, modifier, forks, permission, permissionMessage,
			possibleSources, cooldown);
		this.namespace = namespace;
		this.description = description;
		this.usage = usage;
		if (aliases != null)
			this.aliases.addAll(aliases);
	}

	/**
	 * @return namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @return description
	 */
	public @Nullable String getDescription() {
		return description;
	}

	/**
	 * @return usage
	 */
	public @Nullable CommandUsage getUsage() {
		return usage;
	}

	/**
	 * @return aliases
	 */
	public @Unmodifiable Set<String> getAliases() {
		return aliases;
	}

	public static class Builder<S extends SkriptCommandSender> extends LiteralSkriptCommandNode.Builder<S> {

		/**
		 * Creates new builder for root skript command node with given namespace and name.
		 *
		 * @param namespace namespace of the root command
		 * @param name name of the literal
		 * @return builder
		 * @param <S> command sender type
		 */
		public static <S extends SkriptCommandSender> Builder<S> root(String namespace,
				String name) {
			return new Builder<>(namespace, name);
		}

		private final String namespace;
		private @Nullable String description;
		private @Nullable CommandUsage usage;
		private @Nullable Set<String> aliases = new LinkedHashSet<>();

		protected Builder(String namespace, String literal) {
			super(literal);
			this.namespace = namespace;
		}

		/**
		 * @return literal name
		 */
		public String getNamespace() {
			return namespace;
		}

		/**
		 * @param description new description for command being built
		 * @return this
		 */
		public Builder<S> description(String description) {
			this.description = description;
			return this;
		}

		/**
		 * @return description
		 */
		public @Nullable String getDescription() {
			return description;
		}

		/**
		 * @param usage new usage for command being built
		 * @return this
		 */
		public Builder<S> usage(CommandUsage usage) {
			this.usage = usage;
			return this;
		}

		/**
		 * @return usage
		 */
		public @Nullable CommandUsage getUsage() {
			return usage;
		}

		/**
		 * @param aliases new aliases for command being built
		 * @return this
		 */
		public Builder<S> aliases(String... aliases) {
			if (aliases == null) {
				this.aliases = null;
				return this;
			}
			return aliases(Set.of(aliases));
		}

		/**
		 * @param aliases new aliases for command being built
		 * @return this
		 */
		public Builder<S> aliases(@Nullable Collection<String> aliases) {
			if (aliases == null) {
				this.aliases = null;
				return this;
			}
			this.aliases = new LinkedHashSet<>(aliases);
			return this;
		}

		/**
		 * @return aliases
		 */
		public @Nullable Set<String> getAliases() {
			return aliases;
		}

		@Override
		protected Builder<S> getThis() {
			return this;
		}

		@Override
		public RootSkriptCommandNode<S> build() {
			RootSkriptCommandNode<S> result = new RootSkriptCommandNode<>(getNamespace(),
				getDescription(), getUsage(), getAliases(), getLiteral(), getCommand(),
				getRequirement(), getRedirect(), getRedirectModifier(), isFork(), getPermission(),
				getPermissionMessage(), getPossibleSources(), getCooldown());
			for (CommandNode<S> argument : getArguments())
				result.addChild(argument);
			return result;
		}

	}

}
