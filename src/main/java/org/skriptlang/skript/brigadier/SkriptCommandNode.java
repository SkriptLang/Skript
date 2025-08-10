package org.skriptlang.skript.brigadier;

import org.skriptlang.skript.bukkit.command.PaperCommandUtils;
import ch.njol.skript.lang.VariableString;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.command.CommandCooldown;
import org.skriptlang.skript.lang.command.CommandSourceType;
import org.skriptlang.skript.lang.command.SkriptCommandSender;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Command nodes provided by Skript with additional properties.
 *
 * @param <S> command source
 */
public sealed abstract class SkriptCommandNode<S extends SkriptCommandSender, N extends CommandNode<S>> extends CommandNode<S>
	permits ArgumentSkriptCommandNode, LiteralSkriptCommandNode {

	private final @Nullable String permission;
	private final @Nullable VariableString permissionMessage;
	private final Set<CommandSourceType> possibleSources = new HashSet<>();
	private final @Nullable CommandCooldown cooldown;

	protected SkriptCommandNode(@Nullable Command<S> command, @Nullable Predicate<S> requirement,
			@Nullable CommandNode<S> redirect, @Nullable RedirectModifier<S> modifier, boolean forks,
			@Nullable String permission, @Nullable VariableString permissionMessage,
			@Nullable Collection<CommandSourceType> possibleSources, @Nullable CommandCooldown cooldown) {
		super(command, requirement, redirect, modifier, forks);
		this.permission = permission;
		this.permissionMessage = permissionMessage;
		if (possibleSources != null)
			this.possibleSources.addAll(possibleSources);
		this.cooldown = cooldown;
	}

	/**
	 * @return permission required to execute the command
	 */
	public @Nullable String getPermission() {
		return permission;
	}

	/**
	 * @return message if the command source does not have required permissions to execute the command
	 */
	public @Nullable VariableString getPermissionMessage() {
		return permissionMessage;
	}

	/**
	 * @return possible command source types for this command ({@code executable by} entry)
	 */
	public @Unmodifiable Set<CommandSourceType> getPossibleSources() {
		return Collections.unmodifiableSet(possibleSources);
	}

	/**
	 * @return cooldown of this command
	 */
	public @Nullable CommandCooldown getCooldown() {
		return cooldown;
	}

	/**
	 * Creates native brigadier node from this one.
	 * <p>
	 * Given node must implement the additional properties within its
	 * executable command or requirements.
	 * <p>
	 * This is used for transformation of Skript command nodes to regular Brigadier nodes that
	 * are accepted by platforms such as Paper.
	 *
	 * @return native brigadier node
	 */
	public abstract N flat();

	/**
	 * Creates native brigadier node builder from this node.
	 * <p>
	 * The conversion is done using {@link #flat()}. All the attributes are then copied
	 * to the newly created builder instance.
	 * <p>
	 * This differs from {@link CommandNode#createBuilder()} because it also copies arguments.
	 *
	 * @return native brigadier builder
	 */
	@SuppressWarnings("unchecked")
	public <T extends ArgumentBuilder<S, T>> ArgumentBuilder<S, T> flatToBuilder() {
		return (ArgumentBuilder<S, T>) flatToBuilder(flat());
	}

	@SuppressWarnings("unchecked")
	private static <S extends SkriptCommandSender> ArgumentBuilder<S, ?> flatToBuilder(CommandNode<S> node) {
		ArgumentBuilder<S, ?> builder;
		if (node instanceof LiteralCommandNode<?> lcn) {
			builder = LiteralArgumentBuilder.literal(lcn.getLiteral());
		} else if (node instanceof ArgumentCommandNode<?,?> acn) {
			builder = RequiredArgumentBuilder.argument(acn.getName(), acn.getType());
		} else {
			throw new IllegalStateException("Unsupported command node type; only native brigadier nodes are supported");
		}
		if (node.getRequirement() != null)
			builder.requires(node.getRequirement());
		if (node.getRedirect() != null)
			builder.forward(node.getRedirect(), node.getRedirectModifier(), node.isFork());
		if (node.getCommand() != null)
			builder.executes(node.getCommand());
		for (CommandNode<S> child : node.getChildren()) {
			CommandNode<S> nativeNode = child;
			if (child instanceof SkriptCommandNode<?,?> scn)
				nativeNode = (CommandNode<S>) scn.flat();
			builder.then(flatToBuilder(nativeNode));
		}
		return builder;
	}

	/**
	 * Copies argument node data from given SkriptCommandNode to provided builder and
	 * provides correct implementation for the data unique to SkriptCommandNodes.
	 * <p>
	 * This method is used for simplifying the implementation of {@link #flat()}.
	 *
	 * @param builder builder to copy the data to
	 * @param node SkriptCommandNode to copy (as native brigadier node)
	 * @param <S> command sender type
	 * @param <B> command node builder
	 * @param <F> skript command node to flat
	 */
	@SuppressWarnings("unchecked")
	static <S extends SkriptCommandSender, B extends ArgumentBuilder<S, B>,
			F extends SkriptCommandNode<S, ?>> void flat(B builder, F node) {
		builder.requires(sender -> {
			if (!node.getRequirement().test(sender))
				return false;
			boolean possibleSource = node.getPossibleSources().stream().anyMatch(s -> s.check(null, sender));
			// if there are no specified possible sources, everyone can execute the command
			if (!possibleSource && !node.getPossibleSources().isEmpty())
				return false;
			String permission = node.getPermission();
			return permission == null || permission.isBlank() || sender.hasPermission(permission);
		});

		CommandNode<S> redirect = node.getRedirect();
		if (redirect instanceof SkriptCommandNode<?,?> scn)
			redirect = (CommandNode<S>) scn.flat();
		builder.forward(redirect, node.getRedirectModifier(), node.isFork());

		if (node.getCommand() != null) {
			builder.executes(context -> {
				S sender = context.getSource();
				boolean possibleSource = node.getPossibleSources().stream().anyMatch(s -> s.check(context, sender));
				if (!possibleSource && !node.getPossibleSources().isEmpty()) {
					PaperCommandUtils.sendInvalidExecutorMessage(context);
					return 0;
				}
				String permission = node.getPermission();
				if (permission != null && !permission.isBlank() && !sender.hasPermission(permission)) {
					PaperCommandUtils.sendPermissionMessage(context, node.getPermissionMessage());
					return 0;
				}
				if (!node.getRequirement().test(sender))
					return 0;
				return node.getCommand().run(context);
			});
		}

		for (CommandNode<S> child : node.getChildren()) {
			if (child instanceof SkriptCommandNode<?,?> scn) {
				builder.then((CommandNode<S>) scn.flat());
				continue;
			}
			builder.then(child);
		}
	}

	/**
	 * Represents builder of a SkriptCommandNode.
	 *
	 * @param <S> command sender type
	 * @param <N> native brigadier command node of the SkriptCommandNode
	 * @param <Built> the SkriptCommandNode that is being built
	 * @param <T> this builder
	 */
	public sealed abstract static class Builder<S extends SkriptCommandSender, N extends CommandNode<S>,
			Built extends SkriptCommandNode<S, N>, T extends Builder<S, N, Built, T>>
			extends ArgumentBuilder<S, T> permits ArgumentSkriptCommandNode.Builder, LiteralSkriptCommandNode.Builder {

		private @Nullable String permission = null;
		private @Nullable VariableString permissionMessage = null;
		private final Set<CommandSourceType> possibleSources = new HashSet<>();
		private @Nullable CommandCooldown cooldown = null;

		/**
		 * @param permission new permission for command being built
		 * @return this
		 */
		@Contract("_ -> this")
		public T permission(@Nullable String permission) {
			this.permission = permission;
			return getThis();
		}

		/**
		 * @return permission for command being built
		 */
		public @Nullable String getPermission() {
			return permission;
		}

		/**
		 * @param permissionMessage new permission message for command being built
		 * @return this
		 */
		@Contract("_ -> this")
		public T permissionMessage(@Nullable VariableString permissionMessage) {
			this.permissionMessage = permissionMessage;
			return getThis();
		}

		/**
		 * @return permission message for command being built
		 */
		public @Nullable VariableString getPermissionMessage() {
			return permissionMessage;
		}

		/**
		 * @param possibleSources new possible sources for command being built ({@code executable by})
		 * @return this
		 */
		@Contract("_ -> this")
		public T possibleSources(Collection<CommandSourceType> possibleSources) {
			this.possibleSources.clear();
			this.possibleSources.addAll(possibleSources);
			return getThis();
		}

		/**
		 * @return possible sources for command being built ({@code executable by})
		 */
		public @Unmodifiable Set<CommandSourceType> getPossibleSources() {
			return Set.copyOf(possibleSources);
		}

		/**
		 * @param cooldown new cooldown for command being built
		 * @return this
		 */
		@Contract("_ -> this")
		public T cooldown(@Nullable CommandCooldown cooldown) {
			this.cooldown = cooldown;
			return getThis();
		}

		/**
		 * @return cooldown for command being built
		 */
		public @Nullable CommandCooldown getCooldown() {
			return cooldown;
		}

		/**
		 * Builds the SkriptCommandNode from this builder.
		 *
		 * @return new SkriptCommandNode from this builder
		 */
		@Contract(pure = true)
		public abstract Built build();

	}

}
