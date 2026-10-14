package org.skriptlang.skript.bukkit.command.custom;

import ch.njol.skript.Skript;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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

	private static boolean useSafeReload;
	private static @Nullable MethodHandle SET_VALID;
	private static @Nullable MethodHandle INVALIDATE;
	private static @Nullable MethodHandle REMOVE_COMMAND;
	private static @Nullable MethodHandle SYNC_COMMANDS;

	private static final SkriptIndexHelpTopic indexHelpTopic = new SkriptIndexHelpTopic();

	@ApiStatus.Internal
	public static void init(JavaPlugin plugin) {
		plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
			commandRegistrar = commands.registrar();

			if (!REGISTERED_COMMANDS.isEmpty() || !PENDING_REGISTRATIONS.isEmpty()) {
				REGISTERED_COMMANDS.replaceAll((command, ignored) ->
					commandRegistrar.register(command.node(), command.description(), command.aliases()));
				processRegistrationSet();
				// once command registration has finished, and new help initialized, add in our custom entries
				Bukkit.getScheduler().runTask(plugin, () -> {
					HelpMap helpMap = Bukkit.getHelpMap();
					indexHelpTopic.clear();
					indexHelpTopic.replaceExisting(helpMap);
					CommandMap commandMap = Bukkit.getCommandMap();
					REGISTERED_COMMANDS.forEach((command, labels) ->
						registerHelp(helpMap, commandMap, command, labels));
				});
				return;
			}

			if (ScriptCommandRegistrar.plugin != null) {
				return;
			}
			ScriptCommandRegistrar.plugin = plugin;

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

			// we have to delay replacing the existing help index entry since it does not yet exist
			Bukkit.getScheduler().runTask(plugin, () -> indexHelpTopic.replaceExisting(Bukkit.getHelpMap()));
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
		HelpMap helpMap = Bukkit.getHelpMap();
		CommandMap commandMap = Bukkit.getCommandMap();
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
			if (!useSafeReload) { // safe reloads process help differently at a later time
				registerHelp(helpMap, commandMap, command, REGISTERED_COMMANDS.get(command));
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

	private static void registerHelp(HelpMap helpMap, CommandMap commandMap, ScriptBrigadierCommand command, Set<String> labels) {
		if (useSafeReload) { // remove existing topics, only needed for safe reload which is delayed
			helpMap.getHelpTopics().removeAll(labels.stream()
				.map(label -> {
					// since these topics were created using GenericCommandHelpTopic, a slash is only added as a prefix
					// if the label does not already start with one
					if (label.charAt(0) != '/') {
						label = '/' + label;
					}
					HelpTopic topic = helpMap.getHelpTopic(label);
					if (topic == null && label.charAt(0) == '/') {
						// in some cases, it *is* SkriptGenericCommandHelpTopic - only during shutdown?
						topic = helpMap.getHelpTopic('/' + label);
					}
					return topic;
				})
				.toList());
		}

		// register new help topics
		for (String label : labels) {
			// we prefer accessing the map directly as "getCommand" enforces lowercase
			Command bukkitCommand = commandMap.getKnownCommands().get(label);
			assert bukkitCommand != null;
			if (command.usage() != null) {
				bukkitCommand.setUsage(command.usage());
			}

			HelpTopic newTopic = new SkriptGenericCommandHelpTopic(bukkitCommand);
			helpMap.addTopic(newTopic);
			indexHelpTopic.add(newTopic);
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

			// unregister help
			HelpMap helpMap = Bukkit.getHelpMap();
			List<HelpTopic> topics = PENDING_UNREGISTRATIONS.stream()
				.map(label -> helpMap.getHelpTopic("/" + label))
				.toList();
			helpMap.getHelpTopics().removeAll(topics);
			indexHelpTopic.remove(topics);

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

	/**
	 * @return All commands registered with this registrar.
	 */
	public static Set<ScriptBrigadierCommand> getCommands() {
		return Set.copyOf(REGISTERED_COMMANDS.keySet());
	}

	/**
	 * Utility index help topic to replace the help topic for Skript itself.
	 */
	private static class SkriptIndexHelpTopic extends IndexHelpTopic {

		public SkriptIndexHelpTopic() {
			super("Skript", "All commands for Skript", null,
				new TreeSet<>((lht, rht) -> {
					// always force /skript to come first
					if (lht.getName().equals("/skript")) {
						return -1;
					} else if (rht.getName().equals("/skript")) {
						return 1;
					}
					return HelpTopicComparator.helpTopicComparatorInstance().compare(lht, rht);
				}),
				"Below is a list of all Skript commands:");
		}

		public void replaceExisting(HelpMap helpMap) {
			// replace existing index entry
			helpMap.getHelpTopics().remove(helpMap.getHelpTopic("Skript"));
			helpMap.addTopic(this);
			// add back topic for primary plugin command
			add(helpMap.getHelpTopic("/skript"));
		}

		public void add(HelpTopic topic) {
			allTopics.add(topic);
		}

		public void remove(List<HelpTopic> topics) {
			allTopics.removeAll(topics);
		}

		public void clear() {
			allTopics.clear();
		}

	}

	/**
	 * Utility command help topic to adjust default behavior.
	 */
	private static class SkriptGenericCommandHelpTopic extends GenericCommandHelpTopic {

		public SkriptGenericCommandHelpTopic(Command command) {
			super(command);
			// we need to handle slashes intentionally included as part of the label
			// essentially, prefix with a slash no matter what
			if (command.getLabel().charAt(0) == '/') {
				this.name = "/" + this.name;
			}
		}

	}

}
