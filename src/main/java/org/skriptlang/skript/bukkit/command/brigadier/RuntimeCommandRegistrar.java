package org.skriptlang.skript.bukkit.command.brigadier;

import ch.njol.skript.Skript;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for registering Brigadier commands at runtime through reflection.
 * This avoids a full reload via {@link Bukkit#reloadData()}.
 * However, that approach will be used if the reflection-based approach fails to load.
 */
public final class RuntimeCommandRegistrar {

	/**
	 *
	 * @param node The command node to register.
	 * @param aliases Aliases used to reference the command. Can be empty.
	 * @param description String describing the command.
	 */
	public record CommandRegistration(
		LiteralCommandNode<CommandSourceStack> node,
		Collection<String> aliases,
		@Nullable String description
	) { }

	private static JavaPlugin plugin;

	private static Commands commandRegistrar;
	private static final Map<CommandRegistration, Set<String>> REGISTERED_COMMANDS = new ConcurrentHashMap<>();
	private static final Set<CommandRegistration> PENDING_REGISTRATION = ConcurrentHashMap.newKeySet();
	private static final Set<String> PENDING_UNREGISTRATION = ConcurrentHashMap.newKeySet();

	private static @Nullable MethodHandle SET_VALID;
	private static @Nullable MethodHandle INVALIDATE;
	private static @Nullable MethodHandle REMOVE_COMMAND;
	private static @Nullable MethodHandle SYNC_COMMANDS;

	private static boolean useSafeReload;

	public static void init(JavaPlugin plugin) {
		RuntimeCommandRegistrar.plugin = plugin;
		plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
			commandRegistrar = commands.registrar();

			if (!REGISTERED_COMMANDS.isEmpty() || !PENDING_REGISTRATION.isEmpty()) {
				REGISTERED_COMMANDS.replaceAll((command, ignored) ->
					commandRegistrar.register(command.node, command.description, command.aliases));
				for (CommandRegistration command : PENDING_REGISTRATION) {
					REGISTERED_COMMANDS.put(command, commandRegistrar.register(command.node, command.description, command.aliases));
				}
				PENDING_REGISTRATION.clear();
				return;
			}

			MethodHandles.Lookup lookup = MethodHandles.lookup();
			Class<?> registrarClass = commandRegistrar.getClass();
			try {
				SET_VALID = lookup.findVirtual(registrarClass, "setValid", MethodType.methodType(void.class));
				INVALIDATE = lookup.findVirtual(registrarClass, "invalidate", MethodType.methodType(void.class));
				Class<?> rootClass = commandRegistrar.getDispatcher().getRoot().getClass();
				REMOVE_COMMAND = MethodHandles.privateLookupIn(rootClass, lookup)
					.findVirtual(rootClass, "removeCommand", MethodType.methodType(void.class, String.class));
				SYNC_COMMANDS = lookup.findVirtual(Bukkit.getServer().getClass(), "syncCommands", MethodType.methodType(void.class));
			} catch (NoSuchMethodException | IllegalAccessException e) {
				useSafeReload = true;
			}
		});
	}

	/**
	 * Adds the {@code command} to the registration queue.
	 * To finalize registration, the queue must be processed using {@link #processRegistrations()}.
	 * @param command The command to register.
	 * @see #processRegistrations()
	 */
	public static void register(CommandRegistration command) {
		PENDING_REGISTRATION.add(command);
	}

	/**
	 * Processes all pending registrations, synchronizing them with the server's command dispatcher.
	 * @see #register(CommandRegistration)
	 */
	public static void processRegistrations() {
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(plugin, RuntimeCommandRegistrar::processRegistrations);
			return;
		}

		if (!PENDING_UNREGISTRATION.isEmpty()) {
			processUnregistrations();
		}

		if (useSafeReload) {
			Bukkit.reloadData();
			return;
		}
		assert SET_VALID != null && INVALIDATE != null && SYNC_COMMANDS != null;

		try {
			SET_VALID.invoke(commandRegistrar);
			PluginMeta pluginMeta = plugin.getPluginMeta();
			for (CommandRegistration command : PENDING_REGISTRATION) {
				REGISTERED_COMMANDS.put(command, commandRegistrar.register(pluginMeta, command.node, command.description, command.aliases));
			}
			INVALIDATE.invoke(commandRegistrar);
			SYNC_COMMANDS.invoke(Bukkit.getServer());
		} catch (Throwable e) {
			throw Skript.exception(e);
		}
	}

	/**
	 * Adds the {@code command} to the unregistration queue.
	 * To finalize unregistration, the queue must be processed using {@link #processUnregistrations()}.
	 * @param command The command to unregister.
	 * @see #processUnregistrations()
	 */
	public static void unregister(CommandRegistration command) {
		PENDING_UNREGISTRATION.addAll(REGISTERED_COMMANDS.remove(command));
	}

	/**
	 * Processes all pending unregistrations, synchronizing them with the server's command dispatcher.
	 * @see #unregister(CommandRegistration)
	 */
	public static void processUnregistrations() {
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(plugin, RuntimeCommandRegistrar::processUnregistrations);
			return;
		}

		if (useSafeReload) {
			PENDING_UNREGISTRATION.clear();
			Bukkit.reloadData();
			return;
		}
		assert SET_VALID != null && INVALIDATE != null && REMOVE_COMMAND != null && SYNC_COMMANDS != null;

		try {
			SET_VALID.invoke(commandRegistrar);
			var root = commandRegistrar.getDispatcher().getRoot();
			for (String command : PENDING_UNREGISTRATION) {
				REMOVE_COMMAND.invoke(root, command);
			}
			PENDING_UNREGISTRATION.clear();
			INVALIDATE.invoke(commandRegistrar);
			SYNC_COMMANDS.invoke(Bukkit.getServer());
		} catch (Throwable e) {
			throw Skript.exception(e);
		}
	}

}
