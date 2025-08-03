package org.skriptlang.skript.brigadier;

import ch.njol.skript.lang.VariableString;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.command.CommandCooldown;
import org.skriptlang.skript.lang.command.CommandSourceType;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * SkriptCommandNode implementation of {@link LiteralCommandNode}.
 *
 * @param <S> command sender type
 * @see Builder#literal(String)
 */
public non-sealed class LiteralSkriptCommandNode<S extends CommandSender>
	extends SkriptCommandNode<S, LiteralCommandNode<S>> {

	private final LiteralCommandNode<S> wrapped;

	protected LiteralSkriptCommandNode(String literal, @Nullable Command<S> command, @Nullable Predicate<S> requirement,
			@Nullable CommandNode<S> redirect, @Nullable RedirectModifier<S> modifier, boolean forks,
			@Nullable String permission, @Nullable VariableString permissionMessage,
			@Nullable Collection<CommandSourceType> possibleSources, @Nullable CommandCooldown cooldown) {
		super(command, requirement, redirect, modifier, forks, permission, permissionMessage,
			possibleSources, cooldown);
		this.wrapped = LiteralArgumentBuilder.<S>literal(literal)
			.executes(command)
			.requires(requirement)
			.forward(redirect, modifier, forks)
			.build();
	}

	/**
	 * @return literal of this command node
	 */
	public String getLiteral() {
		return wrapped.getLiteral();
	}

	@Override
	public LiteralCommandNode<S> flat() {
		LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.literal(getLiteral());
		SkriptCommandNode.flat(builder, this);
		return builder.build();
	}

	@Override
	@SuppressWarnings("unchecked")
	public LiteralArgumentBuilder<S> flatToBuilder() {
		return (LiteralArgumentBuilder<S>) super.flatToBuilder();
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
	public void parse(StringReader stringReader,
			CommandContextBuilder<S> commandContextBuilder) throws CommandSyntaxException {
		wrapped.parse(stringReader, commandContextBuilder);
	}

	@Override
	public CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext,
			SuggestionsBuilder suggestionsBuilder) {
		return wrapped.listSuggestions(commandContext, suggestionsBuilder);
	}

	@Override
	public Builder<S> createBuilder() {
		return Builder.<S>literal(getLiteral())
			.executes(getCommand())
			.requires(getRequirement())
			.forward(getRedirect(), getRedirectModifier(), isFork())
			.permission(getPermission())
			.permissionMessage(getPermissionMessage())
			.possibleSources(getPossibleSources())
			.cooldown(getCooldown());
	}

	@Override
	protected String getSortedKey() {
		return getLiteral();
	}

	@Override
	public Collection<String> getExamples() {
		return wrapped.getExamples();
	}

	/**
	 * Builder implementation for {@link LiteralSkriptCommandNode}.
	 *
	 * @param <S> command sender type
	 */
	public static non-sealed class Builder<S extends CommandSender>
		extends SkriptCommandNode.Builder<S, LiteralCommandNode<S>, LiteralSkriptCommandNode<S>, Builder<S>> {

		/**
		 * Creates new builder for literal skript command node with given name.
		 *
		 * @param name name of the literal
		 * @return builder
		 * @param <S> command sender type
		 */
		public static <S extends CommandSender> Builder<S> literal(String name) {
			return new Builder<>(name);
		}

		private final String literal;

		protected Builder(String literal) {
			this.literal = literal;
		}

		/**
		 * @return literal name
		 */
		public String getLiteral() {
			return literal;
		}

		@Override
		protected Builder<S> getThis() {
			return this;
		}

		@Override
		public LiteralSkriptCommandNode<S> build() {
			LiteralSkriptCommandNode<S> result = new LiteralSkriptCommandNode<>(getLiteral(), getCommand(),
				getRequirement(), getRedirect(), getRedirectModifier(), isFork(), getPermission(),
				getPermissionMessage(), getPossibleSources(), getCooldown());
			for (CommandNode<S> argument : getArguments())
				result.addChild(argument);
			return result;
		}

	}

}
