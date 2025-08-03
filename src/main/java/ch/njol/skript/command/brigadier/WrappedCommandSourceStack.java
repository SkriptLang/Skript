package ch.njol.skript.command.brigadier;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * CommandSender instance created from a command source stack.
 *
 * @param sourceStack source stack
 */
// TODO is this needed? probably not and the requirements (Predicate) can have dummy implementation
record WrappedCommandSourceStack(CommandSourceStack sourceStack) implements CommandSender {

	public CommandSender sender() {
		Entity executor = sourceStack.getExecutor();
		return executor != null ? executor : sourceStack.getSender();
	}

	@Override
	public void sendMessage(@NotNull String message) {
		sender().sendMessage(message);
	}

	@Override
	public void sendMessage(@NotNull String... messages) {
		sender().sendMessage(messages);
	}

	@Override
	@Deprecated
	public void sendMessage(@Nullable UUID sender, @NotNull String message) {
		sender().sendMessage(sender, message);
	}

	@Override
	@Deprecated
	public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
		sender().sendMessage(sender, messages);
	}

	@Override
	public @NotNull Server getServer() {
		return sender().getServer();
	}

	@Override
	public @NotNull String getName() {
		return sender().getName();
	}

	@NotNull
	@Override
	public Spigot spigot() {
		return sender().spigot();
	}

	@Override
	public @NotNull Component name() {
		return sender().name();
	}

	@Override
	public boolean isPermissionSet(@NotNull String name) {
		return sender().isPermissionSet(name);
	}

	@Override
	public boolean isPermissionSet(@NotNull Permission perm) {
		return sender().isPermissionSet(perm);
	}

	@Override
	public boolean hasPermission(@NotNull String name) {
		return sender().hasPermission(name);
	}

	@Override
	public boolean hasPermission(@NotNull Permission perm) {
		return sender().hasPermission(perm);
	}

	@Override
	public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
		return sender().addAttachment(plugin, name, value);
	}

	@Override
	public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
		return sender().addAttachment(plugin);
	}

	@Override
	public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
		return sender().addAttachment(plugin, name, value, ticks);
	}

	@Override
	public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
		return sender().addAttachment(plugin, ticks);
	}

	@Override
	public void removeAttachment(@NotNull PermissionAttachment attachment) {
		sender().removeAttachment(attachment);
	}

	@Override
	public void recalculatePermissions() {
		sender().recalculatePermissions();
	}

	@Override
	public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return sender().getEffectivePermissions();
	}

	@Override
	public boolean isOp() {
		return sender().isOp();
	}

	@Override
	public void setOp(boolean value) {
		sender().setOp(value);
	}

}
