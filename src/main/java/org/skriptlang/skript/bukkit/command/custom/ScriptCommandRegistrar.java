package org.skriptlang.skript.bukkit.command.custom;

import ch.njol.skript.Skript;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Utility class for registering Brigadier commands at runtime through reflection.
 * This avoids a full reload via {@link Bukkit#reloadData()}.
 * However, that approach will be used if the reflection-based approach fails to load.
 */
public final class ScriptCommandRegistrar {

	private static JavaPlugin plugin;

	private static Commands commandRegistrar;
	private static final Map<ScriptBrigadierCommand, Set<String>> REGISTERED_COMMANDS = new ConcurrentHashMap<>();
	private static final Set<ScriptBrigadierCommand> PENDING_REGISTRATIONS = ConcurrentHashMap.newKeySet();
	private static final Set<String> PENDING_UNREGISTRATIONS = ConcurrentHashMap.newKeySet();

	private static @Nullable MethodHandle SET_VALID;
	private static @Nullable MethodHandle INVALIDATE;
	private static @Nullable MethodHandle REMOVE_COMMAND;
	private static @Nullable MethodHandle SYNC_COMMANDS;

	private static boolean useSafeReload;

	public static void init(JavaPlugin plugin) {
		ScriptCommandRegistrar.plugin = plugin;
		plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
			commandRegistrar = commands.registrar();

			if (!REGISTERED_COMMANDS.isEmpty() || !PENDING_REGISTRATIONS.isEmpty()) {
				REGISTERED_COMMANDS.replaceAll((command, ignored) ->
					commandRegistrar.register(command.node(), command.description(), command.aliases()));
				processRegistrationSet();
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
	public static void register(ScriptBrigadierCommand command) {
		PENDING_REGISTRATIONS.add(command);
	}

	/**
	 * Processes all pending registrations, synchronizing them with the server's command dispatcher.
	 * @see #register(ScriptBrigadierCommand)
	 */
	public static void processRegistrations() {
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(plugin, ScriptCommandRegistrar::processRegistrations);
			return;
		}

		if (!PENDING_UNREGISTRATIONS.isEmpty()) {
			processUnregistrations();
		}

		if (useSafeReload) {
			Bukkit.reloadData();
			return;
		}
		assert SET_VALID != null && INVALIDATE != null && SYNC_COMMANDS != null;

		try {
			SET_VALID.invoke(commandRegistrar);
			processRegistrationSet();
			INVALIDATE.invoke(commandRegistrar);
			SYNC_COMMANDS.invoke(Bukkit.getServer());
		} catch (Throwable e) {
			throw Skript.exception(e);
		}
	}

	private static void processRegistrationSet() {
		PluginMeta pluginMeta = plugin.getPluginMeta();
		for (ScriptBrigadierCommand command : PENDING_REGISTRATIONS) {
			if (command.namespace() == null) {
				REGISTERED_COMMANDS.put(command, commandRegistrar.register(pluginMeta, command.node(), command.description(), command.aliases()));
			} else {
				TemporaryNamePluginMeta handler = new TemporaryNamePluginMeta(pluginMeta, command.namespace());
				PluginMeta meta = (PluginMeta) Proxy.newProxyInstance(pluginMeta.getClass().getClassLoader(),
					new Class<?>[]{PluginMeta.class}, handler);
				REGISTERED_COMMANDS.put(command, commandRegistrar.register(meta, command.node(), command.description(), command.aliases()));
				handler.useAlternativeName = false;
			}
		}
		PENDING_REGISTRATIONS.clear();
	}

	private static class TemporaryNamePluginMeta implements InvocationHandler {

		final PluginMeta source;
		final String name;
		boolean useAlternativeName = true;

		public TemporaryNamePluginMeta(PluginMeta source, String name) {
			this.source = source;
			this.name = name;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (useAlternativeName) {
				String methodName = method.getName();
				if (methodName.equals("getName") || methodName.equals("namespace")) {
					return name;
				}
			}
			return method.invoke(source, args);
		}

	}

	/**
	 * Adds the {@code command} to the unregistration queue.
	 * To finalize unregistration, the queue must be processed using {@link #processUnregistrations()}.
	 * @param command The command to unregister.
	 * @see #processUnregistrations()
	 */
	public static void unregister(ScriptBrigadierCommand command) {
		PENDING_UNREGISTRATIONS.addAll(REGISTERED_COMMANDS.remove(command));
	}

	/**
	 * Processes all pending unregistrations, synchronizing them with the server's command dispatcher.
	 * @see #unregister(ScriptBrigadierCommand)
	 */
	public static void processUnregistrations() {
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(plugin, ScriptCommandRegistrar::processUnregistrations);
			return;
		}

		if (useSafeReload) {
			PENDING_UNREGISTRATIONS.clear();
			Bukkit.reloadData();
			return;
		}
		assert SET_VALID != null && INVALIDATE != null && REMOVE_COMMAND != null && SYNC_COMMANDS != null;

		try {
			SET_VALID.invoke(commandRegistrar);
			var root = commandRegistrar.getDispatcher().getRoot();
			for (String command : PENDING_UNREGISTRATIONS) {
				REMOVE_COMMAND.invoke(root, command);
			}
			PENDING_UNREGISTRATIONS.clear();
			INVALIDATE.invoke(commandRegistrar);
			SYNC_COMMANDS.invoke(Bukkit.getServer());
		} catch (Throwable e) {
			throw Skript.exception(e);
		}
	}

	/**
	 * Obtains a script command by its name.
	 * @param command The name of the command.
	 * @return The script command named {@code command}, or null if no script command with that name exists.
	 */
	public static @Nullable ScriptBrigadierCommand getCommand(String command) {
		return Stream.concat(PENDING_REGISTRATIONS.stream(), REGISTERED_COMMANDS.keySet().stream())
			.filter(registration -> registration.node().getLiteral().equals(command))
			.findFirst()
			.orElse(null);
	}

}
