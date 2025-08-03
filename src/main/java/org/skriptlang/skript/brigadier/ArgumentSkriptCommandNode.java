package org.skriptlang.skript.brigadier;

import ch.njol.skript.lang.VariableString;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.command.CommandCooldown;
import org.skriptlang.skript.lang.command.CommandSourceType;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * SkriptCommandNode implementation of {@link ArgumentSkriptCommandNode}.
 *
 * @param <S> command sender type
 * @see ArgumentSkriptCommandNode.Builder#argument(String, ArgumentType)
 */
public non-sealed class ArgumentSkriptCommandNode<S extends CommandSender, T>
	extends SkriptCommandNode<S, ArgumentCommandNode<S, T>> {

	private final ArgumentCommandNode<S, T> wrapped;

	public ArgumentSkriptCommandNode(String name, ArgumentType<T> type, @Nullable Command<S> command,
			@Nullable Predicate<S> requirement, @Nullable CommandNode<S> redirect,
			@Nullable RedirectModifier<S> modifier, boolean forks, @Nullable SuggestionProvider<S> customSuggestions,
			@Nullable String permission, @Nullable VariableString permissionMessage,
			@Nullable Collection<CommandSourceType> possibleSources, @Nullable CommandCooldown cooldown) {
		super(command, requirement, redirect, modifier, forks, permission, permissionMessage,
			possibleSources, cooldown);
		wrapped = RequiredArgumentBuilder.<S, T>argument(name, type)
			.executes(command)
			.requires(requirement)
			.forward(redirect, modifier, forks)
			.suggests(customSuggestions)
			.build();
	}

	public ArgumentType<T> getType() {
		return wrapped.getType();
	}

	public @Nullable SuggestionProvider<S> getCustomSuggestions() {
		return wrapped.getCustomSuggestions();
	}

	@Override
	public ArgumentCommandNode<S, T> flat() {
		RequiredArgumentBuilder<S, T> builder = RequiredArgumentBuilder.argument(getName(), getType());
		SkriptCommandNode.flat(builder, this);
		builder.suggests(getCustomSuggestions());
		return builder.build();
	}

	@Override
	@SuppressWarnings("unchecked")
	public RequiredArgumentBuilder<S, T> flatToBuilder() {
		return (RequiredArgumentBuilder<S, T>) super.flatToBuilder();
	}

	@Override
	protected boolean isValidInput(String s) {
		return wrapped.isValidInput(s);
	}

	@Override
	public String getName() {
		return wrapped.getName();
	}

	@Override
	public String getUsageText() {
		return wrapped.getUsageText();
	}

	@Override
	public void parse(StringReader stringReader, CommandContextBuilder<S> commandContextBuilder) throws CommandSyntaxException {
		wrapped.parse(stringReader, commandContextBuilder);
	}

	@Override
	public CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext,
			SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
		return wrapped.listSuggestions(commandContext, suggestionsBuilder);
	}

	@Override
	public Builder<S, T> createBuilder() {
		return Builder.<S, T>argument(getName(), getType())
			.executes(getCommand())
			.requires(getRequirement())
			.forward(getRedirect(), getRedirectModifier(), isFork())
			.suggests(getCustomSuggestions())
			.permission(getPermission())
			.permissionMessage(getPermissionMessage())
			.possibleSources(getPossibleSources())
			.cooldown(getCooldown());
	}

	@Override
	protected String getSortedKey() {
		return getName();
	}

	@Override
	public Collection<String> getExamples() {
		return wrapped.getExamples();
	}

	/**
	 * Builder implementation for {@link ArgumentSkriptCommandNode}.
	 *
	 * @param <S> command sender type
	 * @param <T> argument type
	 */
	public static non-sealed class Builder<S extends CommandSender, T>
		extends SkriptCommandNode.Builder<S, ArgumentCommandNode<S, T>,
		ArgumentSkriptCommandNode<S, T>, Builder<S, T>> {

		/**
		 * Creates new builder for argument skript command node with given name and type.
		 *
		 * @param name name of the argument
		 * @param type type of the argument
		 * @return builder
		 * @param <S> command sender type
		 * @param <T> argument type
		 */
		public static <S extends CommandSender, T> Builder<S, T> argument(String name, ArgumentType<T> type) {
			return new Builder<>(name, type);
		}

		private final String name;
		private final ArgumentType<T> type;
		private @Nullable SuggestionProvider<S> suggestionsProvider;

		protected Builder(String name, ArgumentType<T> type) {
			this.name = name;
			this.type = type;
		}

		/**
		 * @return argument name
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * @return argument type
		 */
		public ArgumentType<T> getType() {
			return this.type;
		}

		/**
		 * @param provider new suggestion provider for command being built
		 * @return this
		 * @see SkriptSuggestionProvider
		 */
		@Contract("_ -> this")
		public Builder<S, T> suggests(SuggestionProvider<S> provider) {
			this.suggestionsProvider = provider;
			return getThis();
		}

		/**
		 * @return suggestion provider for command being built
		 */
		public @Nullable SuggestionProvider<S> getSuggestionsProvider() {
			return suggestionsProvider;
		}

		@Override
		protected Builder<S, T> getThis() {
			return this;
		}

		@Override
		public ArgumentSkriptCommandNode<S, T> build() {
			ArgumentSkriptCommandNode<S, T> result = new ArgumentSkriptCommandNode<>(getName(), getType(), getCommand(),
				getRequirement(), getRedirect(), getRedirectModifier(), isFork(), getSuggestionsProvider(),
				getPermission(), getPermissionMessage(), getPossibleSources(), getCooldown());
			for (CommandNode<S> argument : getArguments())
				result.addChild(argument);
			return result;
		}

	}

}
