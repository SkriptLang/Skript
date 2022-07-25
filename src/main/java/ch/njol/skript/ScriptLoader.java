/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ScriptAliases;
import ch.njol.skript.bukkitutil.CommandReloader;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.TypeHints;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.OpenCloseable;
import ch.njol.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * The main class for loading, unloading and reloading scripts.
 *
 * @author Peter Güttinger
 */
public class ScriptLoader {
	
	private static final Message m_no_errors = new Message("skript.no errors"),
		m_no_scripts = new Message("skript.no scripts");
	private static final PluralizingArgsMessage m_scripts_loaded =
		new PluralizingArgsMessage("skript.scripts loaded");
	
	/**
	 * Clears triggers, commands, functions and variable names
	 */
	static void disableScripts() {
		SkriptEventHandler.removeAllTriggers();
		Commands.clearCommands();
		Functions.clearFunctions();
	}
	
	/**
	 * A class for keeping track of a the general content of a script:
	 * <ul>
	 *     <li>The amount of files</li>
	 *     <li>The amount of triggers</li>
	 *     <li>The amount of commands</li>
	 *     <li>The amount of functions</li>
	 *     <li>The names of the declared commands</li>
	 * </ul>
	 */
	public static class ScriptInfo {
		public int files, triggers, commands, functions;
		
		/**
		 * Command names. They're collected to see if commands need to be
		 * sent to clients on Minecraft 1.13 and newer. Note that add/subtract
		 * don't operate with command names!
		 */
		public final Set<String> commandNames;
		
		public ScriptInfo() {
			commandNames = new HashSet<>();
		}
		
		public ScriptInfo(int numFiles, int numTriggers, int numCommands, int numFunctions) {
			files = numFiles;
			triggers = numTriggers;
			commands = numCommands;
			functions = numFunctions;
			commandNames = new HashSet<>();
		}
		
		/**
		 * Copy constructor.
		 * @param other ScriptInfo to copy from
		 */
		public ScriptInfo(ScriptInfo other) {
			files = other.files;
			triggers = other.triggers;
			commands = other.commands;
			functions = other.functions;
			commandNames = new HashSet<>(other.commandNames);
		}
		
		public void add(ScriptInfo other) {
			files += other.files;
			triggers += other.triggers;
			commands += other.commands;
			functions += other.functions;
		}
		
		public void subtract(ScriptInfo other) {
			files -= other.files;
			triggers -= other.triggers;
			commands -= other.commands;
			functions -= other.functions;
		}
		
		@Override
		public String toString() {
			return "ScriptInfo{files=" + files + ",triggers=" + triggers + ",commands=" + commands + ",functions:" + functions + "}";
		}
	}

	/**
	 * Must be synchronized
	 */
	private static final ScriptInfo loadedScripts = new ScriptInfo();
	
	/**
	 * Command names by script names. Used to figure out when commands need
	 * to be re-sent to clients on MC 1.13+.
	 */
	private static final Map<String, Set<String>> commandNames = new HashMap<>();
	
	/**
	 * @see ParserInstance#get()
	 */
	private static ParserInstance getParser() {
		return ParserInstance.get();
	}
	
	
	/*
	 * Enabled/disabled script tracking
	 */
	/**
	 * All loaded script files.
	 */
	@SuppressWarnings("null")
	private static final Set<File> loadedFiles = Collections.synchronizedSet(new HashSet<>());
	
	/**
	 * Filter for enabled scripts & folders.
	 */
	private static final FileFilter scriptFilter =
		f -> f != null
			&& (f.isDirectory() && !f.getName().startsWith(".") || !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".sk"))
			&& !f.getName().startsWith("-") && !f.isHidden();

	/**
	 * All disabled script files.
	 */
	private static final Set<File> disabledFiles = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Filter for disabled scripts & folders.
	 */
	private static final FileFilter disabledFilter =
		f -> f != null
			&& (f.isDirectory() && !f.getName().startsWith(".") || !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".sk"))
			&& f.getName().startsWith("-") && !f.isHidden();
	
	/**
	 * Reevaluates {@link #disabledFiles}.
	 * @param path the scripts folder to use for the reevaluation.
	 */
	private static void updateDisabledScripts(Path path) {
		disabledFiles.clear();
		try {
			// TODO handle AccessDeniedException
			Files.walk(path)
				.map(Path::toFile)
				.filter(disabledFilter::accept)
				.forEach(disabledFiles::add);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Async loading
	 */
	/**
	 * The tasks that should be executed by the async loaders.
	 * <br>
	 * This queue should only be used when {@link #isAsync()} returns true,
	 * otherwise this queue is not used.
	 * @see AsyncLoaderThread
	 */
	private static final BlockingQueue<Runnable> loadQueue = new LinkedBlockingQueue<>();
	/**
	 * The {@link ThreadGroup} all async loaders belong to.
	 * @see AsyncLoaderThread
	 */
	private static final ThreadGroup asyncLoaderThreadGroup = new ThreadGroup("Skript async loaders");
	/**
	 * All active {@link AsyncLoaderThread}s.
	 */
	private static final List<AsyncLoaderThread> loaderThreads = new ArrayList<>();
	/**
	 * The current amount of loader threads.
	 * <br>
	 * Should always be equal to the size of {@link #loaderThreads},
	 * unless {@link #isAsync()} returns false.
	 * This condition might be false during the execution of {@link #setAsyncLoaderSize(int)}.
	 */
	private static int asyncLoaderSize;
	
	/**
	 * Checks if scripts are loaded in separate thread. If true,
	 * following behavior should be expected:
	 * <ul>
	 *     <li>Scripts are still unloaded and enabled in server thread</li>
	 * 	   <li>When reloading a script, old version is unloaded <i>after</i> it has
	 * 	   been parsed, immediately before it has been loaded</li>
	 * 	   <li>When reloading all scripts, scripts that were removed are disabled
	 * 	   after everything has been reloaded</li>
	 * 	   <li>Script infos returned by most methods are inaccurate</li>
	 * </ul>
	 * @return If main thread is not blocked when loading.
	 */
	public static boolean isAsync() {
		return asyncLoaderSize > 0;
	}
	
	/**
	 * Checks if scripts are loaded in multiple threads instead of one thread.
	 * If true, {@link #isAsync()} will also be true.
	 * @return if parallel loading is enabled.
	 */
	public static boolean isParallel() {
		return asyncLoaderSize > 1;
	}
	
	/**
	 * Sets the amount of async loaders, by updating
	 * {@link #asyncLoaderSize} and {@link #loaderThreads}.
	 * <br>
	 * If {@code size <= 0}, async and parallel loading are disabled.
	 * <br>
	 * If {@code size == 1}, async loading is enabled but parallel loading is disabled.
	 * <br>
	 * If {@code size >= 2}, async and parallel loading are enabled.
	 *
	 * @param size the amount of async loaders to use.
	 */
	public static void setAsyncLoaderSize(int size) throws IllegalStateException {
		asyncLoaderSize = size;
		if (size <= 0) {
			for (AsyncLoaderThread thread : loaderThreads)
				thread.cancelExecution();
			return;
		}
		
		// Remove threads
		while (loaderThreads.size() > size) {
			AsyncLoaderThread thread = loaderThreads.remove(loaderThreads.size() - 1);
			thread.cancelExecution();
		}
		// Add threads
		while (loaderThreads.size() < size) {
			loaderThreads.add(AsyncLoaderThread.create());
		}
		
		if (loaderThreads.size() != size)
			throw new IllegalStateException();
	}
	
	/**
	 * This thread takes and executes tasks from the {@link #loadQueue}.
	 * Instances of this class must be created with {@link AsyncLoaderThread#create()},
	 * and created threads will always be part of the {@link #asyncLoaderThreadGroup}.
	 */
	private static class AsyncLoaderThread extends Thread {
		
		/**
		 * @see AsyncLoaderThread
		 */
		public static AsyncLoaderThread create() {
			AsyncLoaderThread thread = new AsyncLoaderThread();
			thread.start();
			return thread;
		}
		
		private AsyncLoaderThread() {
			super(asyncLoaderThreadGroup, (Runnable) null);
		}
		
		private boolean shouldRun = true;
		
		@Override
		public void run() {
			while (shouldRun) {
				try {
					Runnable runnable = loadQueue.poll(100, TimeUnit.MILLISECONDS);
					if (runnable != null)
						runnable.run();
				} catch (InterruptedException e) {
					//noinspection ThrowableNotThrown
					Skript.exception(e); // Bubble it up with instructions on how to report it
				}
			}
		}
		
		/**
		 * Tell the loader it should stop taking tasks.
		 * <br>
		 * If this thread is currently executing a task, it will stop when that task is done.
		 * <br>
		 * If this thread is not executing a task,
		 * it is stopped after at most 100 milliseconds.
		 */
		public void cancelExecution() {
			shouldRun = false;
		}
		
	}
	
	/**
	 * Creates a {@link CompletableFuture} using a {@link Supplier} and an {@link OpenCloseable}.
	 * <br>
	 * The {@link Runnable} of this future should not throw any exceptions,
	 * since it catches all exceptions thrown by the {@link Supplier} and {@link OpenCloseable}.
	 * <br>
	 * If no exceptions are thrown, the future is completed by
	 * calling {@link OpenCloseable#open()}, then {@link Supplier#get()}
	 * followed by {@link OpenCloseable#close()}, where the result value is
	 * given by the supplier call.
	 * <br>
	 * If an exception is thrown, the future is completed exceptionally with the caught exception,
	 * and {@link Skript#exception(Throwable, String...)} is called.
	 * <br>
	 * The future is executed on an async loader thread, only if
	 * both {@link #isAsync()} and {@link Bukkit#isPrimaryThread()} return true,
	 * otherwise this future is executed immediately, and the returned future is already completed.
	 *
	 * @return a {@link CompletableFuture} of the type specified by
	 * the generic of the {@link Supplier} parameter.
	 */
	private static <T> CompletableFuture<T> makeFuture(Supplier<T> supplier, OpenCloseable openCloseable) {
		CompletableFuture<T> future = new CompletableFuture<>();
		Runnable task = () -> {
			try {
				openCloseable.open();
				T t;
				try {
					t = supplier.get();
				} finally {
					openCloseable.close();
				}
				
				future.complete(t);
			} catch (Throwable t) {
				future.completeExceptionally(t);
				//noinspection ThrowableNotThrown
				Skript.exception(t);
			}
		};
		
		if (isAsync() && Bukkit.isPrimaryThread()) {
			loadQueue.add(task);
		} else {
			task.run();
			assert future.isDone();
		}
		return future;
	}
	
	
	/*
	 * Script loading methods
	 */
	/**
	 * Loads all scripts in the scripts folder using {@link #loadScripts(List, OpenCloseable)},
	 * sending info/error messages when done.
	 */
	static CompletableFuture<Void> loadScripts(OpenCloseable openCloseable) {
		File scriptsFolder = new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER + File.separator);
		if (!scriptsFolder.isDirectory())
			//noinspection ResultOfMethodCallIgnored
			scriptsFolder.mkdirs();
		
		Date start = new Date();
		
		updateDisabledScripts(scriptsFolder.toPath());
		
		Set<File> oldLoadedFiles = new HashSet<>(loadedFiles);
		
		List<Config> configs;
		
		CountingLogHandler logHandler = new CountingLogHandler(Level.SEVERE).start();
		try {
			configs = loadStructures(scriptsFolder);
		} finally {
			logHandler.stop();
		}
		
		return loadScripts(configs, OpenCloseable.combine(openCloseable, logHandler))
			.thenAccept(scriptInfo -> {
				// Success
				if (logHandler.getCount() == 0)
					Skript.info(m_no_errors.toString());
				
				// Now, make sure that old files that are no longer there are unloaded
				// Only if this is done using async loading, though!
				if (isAsync()) {
					oldLoadedFiles.removeAll(loadedFiles);
					for (File script : oldLoadedFiles) {
						if (script == null)
							throw new NullPointerException();
						
						// Use internal unload method which does not call validateFunctions()
						unloadScript_(script);
						String name = Skript.getInstance().getDataFolder().toPath().toAbsolutePath()
							.resolve(Skript.SCRIPTSFOLDER).relativize(script.toPath()).toString();
						assert name != null;
						Functions.clearFunctions(name);
					}
					Functions.validateFunctions(); // Manually validate functions
				}
				
				if (scriptInfo.files == 0)
					Skript.warning(m_no_scripts.toString());
				if (Skript.logNormal() && scriptInfo.files > 0)
					Skript.info(m_scripts_loaded.toString(
						scriptInfo.files,
						scriptInfo.triggers,
						scriptInfo.commands,
						start.difference(new Date())
					));
			});
	}
	
	/**
	 * Loads the specified scripts.
	 *
	 * @param configs Configs for scripts, loaded by {@link #loadStructures(File[])}
	 * @param openCloseable An {@link OpenCloseable} that will be called before and after
	 *                         each individual script load (see {@link #makeFuture(Supplier, OpenCloseable)}).
	 * @return Info on the loaded scripts.
	 */
	public static CompletableFuture<ScriptInfo> loadScripts(List<Config> configs, OpenCloseable openCloseable) {
		AtomicBoolean syncCommands = new AtomicBoolean();
    
		Bukkit.getPluginManager().callEvent(new PreScriptLoadEvent(configs));
		
		ScriptInfo scriptInfo = new ScriptInfo();
		
		List<CompletableFuture<Void>> scriptInfoFutures = new ArrayList<>();
		for (Config config : configs) {
			if (config == null)
				throw new NullPointerException();
			
			CompletableFuture<Void> future = makeFuture(() -> {
				ScriptInfo info = loadScript(config);
				
				// Check if commands have been changed and a re-send is needed
				if (!info.commandNames.equals(commandNames.get(config.getFileName()))) {
					syncCommands.set(true); // Sync once after everything has been loaded
					commandNames.put(config.getFileName(), info.commandNames); // These will soon be sent to clients
				}
				
				scriptInfo.add(info);
				return null;
			}, openCloseable);
			
			scriptInfoFutures.add(future);
		}
		
		return CompletableFuture.allOf(scriptInfoFutures.toArray(new CompletableFuture[0]))
			.thenApply(unused -> {
				SkriptEventHandler.registerBukkitEvents();
				
				// After we've loaded everything, refresh commands their names changed
				if (syncCommands.get()) {
					if (CommandReloader.syncCommands(Bukkit.getServer()))
						Skript.debug("Commands synced to clients");
					else
						Skript.debug("Commands changed but not synced to clients (normal on 1.12 and older)");
				} else {
					Skript.debug("Commands unchanged, not syncing them to clients");
				}
				
				return scriptInfo;
			});
	}
	
	/**
	 * Represents data for event which is waiting to be loaded.
	 */
	private static class ParsedEventData {
		public final NonNullPair<SkriptEventInfo<?>, SkriptEvent> info;
		public final String event;
		public final SectionNode node;
		public final List<TriggerItem> items;
		
		public ParsedEventData(NonNullPair<SkriptEventInfo<?>, SkriptEvent> info,
							   String event,
							   SectionNode node,
							   List<TriggerItem> items) {
			this.info = info;
			this.event = event;
			this.node = node;
			this.items = items;
		}
	}
	
	/**
	 * Loads one script. Only for internal use, as this doesn't register/update
	 * event handlers.
	 * @param config Config for script to be loaded.
	 * @return Info about script that is loaded
	 */
	// Whenever you call this method, make sure to also call PreScriptLoadEvent
	private static ScriptInfo loadScript(@Nullable Config config) {
		if (config == null) { // Something bad happened, hopefully got logged to console
			return new ScriptInfo();
		}

		// When something is parsed, it goes there to be loaded later
		List<ScriptCommand> commands = new ArrayList<>();
		List<ParsedEventData> events = new ArrayList<>();
		
		// Track what is loaded
		ScriptInfo scriptInfo = new ScriptInfo();
		scriptInfo.files = 1; // Loading one script
		
		try {
			if (SkriptConfig.keepConfigsLoaded.value())
				SkriptConfig.configs.add(config);
			
			getParser().getCurrentOptions().clear();
			getParser().setCurrentScript(config);
			
			try (CountingLogHandler ignored = new CountingLogHandler(SkriptLogger.SEVERE).start()) {
				for (Node cnode : config.getMainNode()) {
					if (!(cnode instanceof SectionNode)) {
						Skript.error("invalid line - all code has to be put into triggers");
						continue;
					}
					
					SectionNode node = ((SectionNode) cnode);
					String event = node.getKey();
					if (event == null)
						continue;
					
					if (event.equalsIgnoreCase("aliases")) {
						node.convertToEntries(0, "=");
						
						// Initialize and load script aliases
						ScriptAliases aliases = Aliases.createScriptAliases();
						Aliases.setScriptAliases(aliases);
						aliases.parser.load(node);
						continue;
					} else if (event.equalsIgnoreCase("options")) {
						node.convertToEntries(0);
						for (Node n : node) {
							if (!(n instanceof EntryNode)) {
								Skript.error("invalid line in options");
								continue;
							}
							getParser().getCurrentOptions().put(n.getKey(), ((EntryNode) n).getValue());
						}
						continue;
					} else if (event.equalsIgnoreCase("variables")) {
						// TODO allow to make these override existing variables
						node.convertToEntries(0, "=");
						for (Node n : node) {
							if (!(n instanceof EntryNode)) {
								Skript.error("Invalid line in variables section");
								continue;
							}
							String name = n.getKey().toLowerCase(Locale.ENGLISH);
							if (name.startsWith("{") && name.endsWith("}"))
								name = "" + name.substring(1, name.length() - 1);
							String var = name;
							name = StringUtils.replaceAll(name, "%(.+)?%", m -> {
								if (m.group(1).contains("{") || m.group(1).contains("}") || m.group(1).contains("%")) {
									Skript.error("'" + var + "' is not a valid name for a default variable");
									return null;
								}
								ClassInfo<?> ci = Classes.getClassInfoFromUserInput("" + m.group(1));
								if (ci == null) {
									Skript.error("Can't understand the type '" + m.group(1) + "'");
									return null;
								}
								return "<" + ci.getCodeName() + ">";
							});
							if (name == null) {
								continue;
							} else if (name.contains("%")) {
								Skript.error("Invalid use of percent signs in variable name");
								continue;
							}
							if (Variables.getVariable(name, null, false) != null)
								continue;
							Object o;
							ParseLogHandler log = SkriptLogger.startParseLogHandler();
							try {
								o = Classes.parseSimple(((EntryNode) n).getValue(), Object.class, ParseContext.SCRIPT);
								if (o == null) {
									log.printError("Can't understand the value '" + ((EntryNode) n).getValue() + "'");
									continue;
								}
								log.printLog();
							} finally {
								log.stop();
							}
							ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
							if (ci.getSerializer() == null) {
								Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
								continue;
							} else if (ci.getSerializeAs() != null) {
								ClassInfo<?> as = Classes.getExactClassInfo(ci.getSerializeAs());
								if (as == null) {
									assert false : ci;
									continue;
								}
								o = Converters.convert(o, as.getC());
								if (o == null) {
									Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
									continue;
								}
							}
							Variables.setVariable(name, o, null, false);
						}
						continue;
					}
					
					if (!SkriptParser.validateLine(event))
						continue;
					
					if (event.toLowerCase(Locale.ENGLISH).startsWith("command ")) {
						
						getParser().setCurrentEvent("command", CommandEvent.class);
						
						ScriptCommand c = Commands.loadCommand(node, false);
						if (c != null) {
							commands.add(c);
							scriptInfo.commandNames.add(c.getName()); // For tab completion
							scriptInfo.commands++;
						}
						
						getParser().deleteCurrentEvent();
						
						continue;
					} else if (event.toLowerCase(Locale.ENGLISH).startsWith("function ")) {
						
						getParser().setCurrentEvent("function", FunctionEvent.class);
						
						Function<?> func = Functions.loadFunction(node);
						if (func != null) {
							scriptInfo.functions++;
						}
						
						getParser().deleteCurrentEvent();
						
						continue;
					}
					
					if (Skript.logVeryHigh() && !Skript.debug())
						Skript.info("loading trigger '" + event + "'");
					
					if (StringUtils.startsWithIgnoreCase(event, "on "))
						event = "" + event.substring("on ".length());
					
					event = replaceOptions(event);
					
					NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(event, "Can't understand this event: '" + node.getKey() + "'");
					if (parsedEvent == null || !parsedEvent.getSecond().shouldLoadEvent())
						continue;
					
					if (Skript.debug() || node.debug())
						Skript.debug(SkriptColor.replaceColorChar(event + " (" + parsedEvent.getSecond().toString(null, true) + "):"));

					Class<? extends Event>[] eventClasses = parsedEvent.getSecond().getEventClasses();
					if (eventClasses == null)
						eventClasses = parsedEvent.getFirst().events;
					try {
						getParser().setCurrentEvent(parsedEvent.getFirst().getName().toLowerCase(Locale.ENGLISH), eventClasses);
						getParser().setCurrentSkriptEvent(parsedEvent.getSecond());
						events.add(new ParsedEventData(parsedEvent, event, node, loadItems(node)));
					} finally {
						getParser().deleteCurrentEvent();
						getParser().deleteCurrentSkriptEvent();
					}
					
					if (parsedEvent.getSecond() instanceof SelfRegisteringSkriptEvent) {
						((SelfRegisteringSkriptEvent) parsedEvent.getSecond()).afterParse(config);
					}
					
					scriptInfo.triggers++;
				}
				
				if (Skript.logHigh())
					Skript.info("loaded " + scriptInfo.triggers + " trigger" + (scriptInfo.triggers == 1 ? "" : "s")+ " and " + scriptInfo.commands + " command" + (scriptInfo.commands == 1 ? "" : "s") + " from '" + config.getFileName() + "'");
				
				getParser().setCurrentScript(null);
				Aliases.setScriptAliases(null); // These are per-script
			}
		} catch (Exception e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "Could not load " + config.getFileName());
		} finally {
			SkriptLogger.setNode(null);
		}
		
		// In always sync task, enable stuff
		Callable<Void> callable = () -> {
			// Unload script IF we're doing async stuff
			// (else it happened already)
			File file = config.getFile();
			if (isAsync()) {
				if (file != null)
					unloadScript_(file);
			}
			
			// Now, enable everything!
			for (ScriptCommand command : commands) {
				Commands.registerCommand(command);
			}
			
			for (ParsedEventData event : events) {
				Class<? extends Event>[] eventClasses = event.info.getSecond().getEventClasses();
				if (eventClasses == null)
					eventClasses = event.info.getFirst().events;
				getParser().setCurrentEvent(event.info.getFirst().getName().toLowerCase(Locale.ENGLISH), eventClasses);
				getParser().setCurrentSkriptEvent(event.info.getSecond());
				
				Trigger trigger;
				try {
					trigger = new Trigger(config.getFile(), event.event, event.info.getSecond(), event.items);
					trigger.setLineNumber(event.node.getLine()); // Set line number for debugging
					trigger.setDebugLabel(config.getFileName() + ": line " + event.node.getLine());
				} finally {
					getParser().deleteCurrentEvent();
				}
				
				if (event.info.getSecond() instanceof SelfRegisteringSkriptEvent) {
					((SelfRegisteringSkriptEvent) event.info.getSecond()).register(trigger);
					SkriptEventHandler.addSelfRegisteringTrigger(trigger);
				} else {
					SkriptEventHandler.addTrigger(event.info.getFirst().events, trigger);
				}
				
				getParser().deleteCurrentEvent();
				getParser().deleteCurrentSkriptEvent();
			}
			
			// Remove the script from the disabled scripts list
			File disabledFile = new File(file.getParentFile(), "-" + file.getName());
			disabledFiles.remove(disabledFile);
			
			// Add to loaded files to use for future reloads
			loadedFiles.add(file);
			
			return null;
		};
		if (isAsync()) { // Need to delegate to main thread
			Task.callSync(callable);
		} else { // We are in main thread, execute immediately
			try {
				callable.call();
			} catch (Exception e) {
				//noinspection ThrowableNotThrown
				Skript.exception(e);
			}
		}
		
		return scriptInfo;
	}
	
	
	/*
	 * Structure loading methods
	 */
	/**
	 * Loads structures of specified scripts.
	 *
	 * @param files the scripts to load
	 */
	public static List<Config> loadStructures(File[] files) {
		Arrays.sort(files);
		
		List<Config> loadedFiles = new ArrayList<>(files.length);
		for (File f : files) {
			assert f != null : Arrays.toString(files);
			Config config = loadStructure(f);
			if (config != null)
				loadedFiles.add(config);
		}
		
		return loadedFiles;
	}
	
	/**
	 * Loads structures of all scripts in the given directory, or of the passed script if it's a normal file.
	 *
	 * @param directory a directory or a single file
	 * @see #loadStructure(File).
	 */
	public static List<Config> loadStructures(File directory) {
		if (!directory.isDirectory()) {
			Config config = loadStructure(directory);
			return config != null ? Collections.singletonList(config) : Collections.emptyList();
		}
		
		File[] files = directory.listFiles(scriptFilter);
		Arrays.sort(files);
		
		List<Config> loadedDirectories = new ArrayList<>(files.length);
		List<Config> loadedFiles = new ArrayList<>(files.length);
		for (File file : files) {
			if (file.isDirectory()) {
				loadedDirectories.addAll(loadStructures(file));
			} else {
				Config cfg = loadStructure(file);
				if (cfg != null)
					loadedFiles.add(cfg);
			}
		}

		loadedDirectories.addAll(loadedFiles);
		return loadedDirectories;
	}
	
	/**
	 * Loads structure of given script, currently only for functions. Must be called before
	 * actually loading that script.
	 * @param f Script file.
	 */
	@SuppressWarnings("resource") // Stream is closed in Config constructor called in loadStructure
	@Nullable
	public static Config loadStructure(File f) {
		if (!f.exists()) { // If file does not exist...
			unloadScript(f); // ... it might be good idea to unload it now
			return null;
		}
		
		try {
			String name = Skript.getInstance().getDataFolder().toPath().toAbsolutePath()
					.resolve(Skript.SCRIPTSFOLDER).relativize(f.toPath().toAbsolutePath()).toString();
			assert name != null;
			return loadStructure(new FileInputStream(f), name);
		} catch (IOException e) {
			Skript.error("Could not load " + f.getName() + ": " + ExceptionUtils.toString(e));
		}
		
		return null;
	}
	
	/**
	 * Loads structure of given script, currently only for functions. Must be called before
	 * actually loading that script.
	 * @param source Source input stream.
	 * @param name Name of source "file".
	 */
	@Nullable
	public static Config loadStructure(InputStream source, String name) {
		try {
			Config config = new Config(
				source,
				name,
				Skript.getInstance().getDataFolder().toPath().resolve(Skript.SCRIPTSFOLDER).resolve(name).toFile(),
				true,
				false,
				":"
			);
			return loadStructure(config);
		} catch (IOException e) {
			Skript.error("Could not load " + name + ": " + ExceptionUtils.toString(e));
		}
		
		return null;
	}
	
	/**
	 * Loads structure of given script, currently only for functions. Must be called before
	 * actually loading that script.
	 * @param config Config object for the script.
	 */
	@Nullable
	public static Config loadStructure(Config config) {
		try {
			for (Node cnode : config.getMainNode()) {
				if (!(cnode instanceof SectionNode)) {
					// Don't spit error yet, we are only pre-parsing...
					continue;
				}
				
				SectionNode node = ((SectionNode) cnode);
				String event = node.getKey();
				if (event == null)
					continue;
				
				if (!SkriptParser.validateLine(event))
					continue;
				
				if (event.toLowerCase(Locale.ENGLISH).startsWith("function ")) {
					
					getParser().setCurrentEvent("function", FunctionEvent.class);
					
					Functions.loadSignature(config.getFileName(), node);
					
					getParser().deleteCurrentEvent();
				}
			}
			
			getParser().setCurrentScript(null);
			SkriptLogger.setNode(null);
			return config;
		} catch (Exception e) {
			Skript.exception(e, "Could not load " + config.getFileName());
		} finally {
			SkriptLogger.setNode(null);
		}
		return null; // Oops something went wrong
	}
	
	
	/*
	 * Script unloading methods
	 */
	/**
	 * Unloads the scripts in a folder.
	 * @return The {@link ScriptInfo} of all unloaded scripts combined.
	 */
	private static ScriptInfo unloadScripts_(File folder) {
		ScriptInfo info = new ScriptInfo();
		for (File f : folder.listFiles(scriptFilter)) {
			if (f.isDirectory()) {
				info.add(unloadScripts_(f));
			} else {
				info.add(unloadScript_(f));
			}
		}
		return info;
	}
	
	/**
	 * Unloads the specified script.
	 *
	 * @param script
	 * @return Info on the unloaded script
	 */
	public static ScriptInfo unloadScript(File script) {
		ScriptInfo r = unloadScript_(script);
		Functions.validateFunctions();
		return r;
	}
	
	private static ScriptInfo unloadScript_(File script) {
		if (loadedFiles.contains(script)) {
			ScriptInfo info = SkriptEventHandler.removeTriggers(script); // Remove triggers
			synchronized (loadedScripts) { // Update global script info
				loadedScripts.subtract(info);
			}
			
			loadedFiles.remove(script); // We just unloaded it, so...
			disabledFiles.add(new File(script.getParentFile(), "-" + script.getName()));
			
			// Clear functions, DO NOT validate them yet
			// If unloading, our caller will do this immediately after we return
			// However, if reloading, new version of this script is first loaded
			String name = Skript.getInstance().getDataFolder().toPath().toAbsolutePath()
					.resolve(Skript.SCRIPTSFOLDER).relativize(script.toPath().toAbsolutePath()).toString();
			assert name != null;
			Functions.clearFunctions(name);
			
			return info; // Return how much we unloaded
		}
		
		return new ScriptInfo(); // Return that we unloaded literally nothing
	}
	
	
	/*
	 * Script reloading methods
	 */
	/**
	 * Reloads a single script.
	 * @param script Script file.
	 * @return Future of statistics of the newly loaded script.
	 */
	public static CompletableFuture<ScriptInfo> reloadScript(File script, OpenCloseable openCloseable) {
		if (!isAsync()) {
			unloadScript_(script);
		}
		Config config = loadStructure(script);
		Functions.validateFunctions();
		if (config == null)
			return CompletableFuture.completedFuture(new ScriptInfo());
		return loadScripts(Collections.singletonList(config), openCloseable);
	}
	
	/**
	 * Reloads all scripts in the given folder and its subfolders.
	 * @param folder A folder.
	 * @return Future of statistics of newly loaded scripts.
	 */
	public static CompletableFuture<ScriptInfo> reloadScripts(File folder, OpenCloseable openCloseable) {
		if (!isAsync()) {
			unloadScripts_(folder);
		}
		List<Config> configs = loadStructures(folder);
		Functions.validateFunctions();
		return loadScripts(configs, openCloseable);
	}

	
	/*
	 * Code loading methods
	 */
	/**
	 * Replaces options in a string.
	 */
	public static String replaceOptions(String s) {
		String r = StringUtils.replaceAll(s, "\\{@(.+?)\\}", m -> {
			String option = getParser().getCurrentOptions().get(m.group(1));
			if (option == null) {
				Skript.error("undefined option " + m.group());
				return m.group();
			}
			return Matcher.quoteReplacement(option);
		});
		assert r != null;
		return r;
	}
	
	/**
	 * Loads a section by converting it to {@link TriggerItem}s.
	 */
	public static ArrayList<TriggerItem> loadItems(SectionNode node) {
		if (Skript.debug())
			getParser().setIndentation(getParser().getIndentation() + "    ");
		
		ArrayList<TriggerItem> items = new ArrayList<>();

		for (Node n : node) {
			SkriptLogger.setNode(n);
			if (n instanceof SimpleNode) {
				String expr = replaceOptions("" + n.getKey());
				if (!SkriptParser.validateLine(expr))
					continue;

				long start = System.currentTimeMillis();
				Statement stmt = Statement.parse(expr, "Can't understand this condition/effect: " + expr);
				if (stmt == null)
					continue;
				long requiredTime = SkriptConfig.longParseTimeWarningThreshold.value().getMilliSeconds();
				if (requiredTime > 0) {
					long timeTaken = System.currentTimeMillis() - start;
					if (timeTaken > requiredTime)
						Skript.warning(
							"The current line took a long time to parse (" + new Timespan(timeTaken) + ")."
								+ " Avoid using long lines and use parentheses to create clearer instructions."
						);
				}

				if (Skript.debug() || n.debug())
					Skript.debug(SkriptColor.replaceColorChar(getParser().getIndentation() + stmt.toString(null, true)));

				items.add(stmt);
			} else if (n instanceof SectionNode) {
				String expr = replaceOptions("" + n.getKey());
				if (!SkriptParser.validateLine(expr))
					continue;
				TypeHints.enterScope(); // Begin conditional type hints

				Section section = Section.parse(expr, "Can't understand this section: " + expr, (SectionNode) n, items);
				if (section == null)
					continue;

				if (Skript.debug() || n.debug())
					Skript.debug(SkriptColor.replaceColorChar(getParser().getIndentation() + section.toString(null, true)));

				items.add(section);

				// Destroy these conditional type hints
				TypeHints.exitScope();
			}
		}
		
		for (int i = 0; i < items.size() - 1; i++)
			items.get(i).setNext(items.get(i + 1));
		
		SkriptLogger.setNode(node);
		
		if (Skript.debug())
			getParser().setIndentation("" + getParser().getIndentation().substring(0, getParser().getIndentation().length() - 4));
		
		return items;
	}
	
	/**
	 * For unit testing
	 *
	 * @param node
	 * @return The loaded Trigger
	 */
	@Nullable
	static Trigger loadTrigger(SectionNode node) {
		String event = node.getKey();
		if (event == null) {
			assert false : node;
			return null;
		}
		if (event.toLowerCase(Locale.ENGLISH).startsWith("on "))
			event = "" + event.substring("on ".length());
		
		NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent =
			SkriptParser.parseEvent(event, "Can't understand this event: '" + node.getKey() + "'");
		if (parsedEvent == null) {
			assert false;
			return null;
		}
		
		getParser().setCurrentEvent("unit test", parsedEvent.getFirst().events);
		try {
			return new Trigger(null, event, parsedEvent.getSecond(), loadItems(node));
		} finally {
			getParser().deleteCurrentEvent();
		}
	}
	
	
	/*
	 * Loaded script statistics
	 */
	@SuppressWarnings("null") // Collections methods don't return nulls, ever
	public static Collection<File> getLoadedFiles() {
		return Collections.unmodifiableCollection(loadedFiles);
	}
	
	@SuppressWarnings("null")
	public static Collection<File> getDisabledFiles() {
		return Collections.unmodifiableCollection(disabledFiles);
	}
	
	public static int loadedScripts() {
		synchronized (loadedScripts) {
			return loadedScripts.files;
		}
	}
	
	public static int loadedCommands() {
		synchronized (loadedScripts) {
			return loadedScripts.commands;
		}
	}
	
	public static int loadedFunctions() {
		synchronized (loadedScripts) {
			return loadedScripts.functions;
		}
	}
	
	public static int loadedTriggers() {
		synchronized (loadedScripts) {
			return loadedScripts.triggers;
		}
	}
	
	
	/*
	 * Deprecated stuff
	 *
	 * These fields / methods are from the old version of ScriptLoader,
	 * and are merely here for backwards compatibility.
	 *
	 * Some methods have been replaced by ParserInstance, some
	 * by new methods in this class.
	 */
	/**
	 * @see #loadScripts(OpenCloseable)
	 */
	@Deprecated
	static void loadScripts() {
		if (!isAsync())
			disableScripts();
		loadScripts(OpenCloseable.EMPTY).join();
	}
	
	/**
	 * @see #loadScripts(List, OpenCloseable)
	 */
	@Deprecated
	public static ScriptInfo loadScripts(List<Config> configs) {
		return loadScripts(configs, OpenCloseable.EMPTY).join();
	}
	
	/**
	 * @see #loadScripts(List, OpenCloseable)
	 * @see RetainingLogHandler
	 */
	@Deprecated
	public static ScriptInfo loadScripts(List<Config> configs, List<LogEntry> logOut) {
		RetainingLogHandler logHandler = new RetainingLogHandler();
		try {
			return loadScripts(configs, logHandler).join();
		} finally {
			logOut.addAll(logHandler.getLog());
		}
	}
	
	/**
	 * @see #loadScripts(List, OpenCloseable)
	 */
	@Deprecated
	public static ScriptInfo loadScripts(Config... configs) {
		return loadScripts(Arrays.asList(configs), OpenCloseable.EMPTY).join();
	}
	
	/**
	 * @see #reloadScript(File, OpenCloseable)
	 */
	@Deprecated
	public static ScriptInfo reloadScript(File script) {
		return reloadScript(script, OpenCloseable.EMPTY).join();
	}
	
	/**
	 * @see #reloadScripts(File, OpenCloseable)
	 */
	@Deprecated
	public static ScriptInfo reloadScripts(File folder) {
		return reloadScripts(folder, OpenCloseable.EMPTY).join();
	}
	
	/**
	 * @see ParserInstance#getHasDelayBefore()
	 */
	@Deprecated
	public static Kleenean getHasDelayBefore() {
		return getParser().getHasDelayBefore();
	}
	
	/**
	 * @see ParserInstance#setHasDelayBefore(Kleenean)
	 */
	@Deprecated
	public static void setHasDelayBefore(Kleenean hasDelayBefore) {
		getParser().setHasDelayBefore(hasDelayBefore);
	}
	
	/**
	 * @see ParserInstance#getCurrentScript()
	 */
	@Nullable
	@Deprecated
	public static Config getCurrentScript() {
		return getParser().getCurrentScript();
	}
	
	/**
	 * @see ParserInstance#setCurrentScript(Config)
	 */
	@Deprecated
	public static void setCurrentScript(@Nullable Config currentScript) {
		getParser().setCurrentScript(currentScript);
	}
	
	/**
	 * @see ParserInstance#getCurrentSections()
	 */
	@Deprecated
	public static List<TriggerSection> getCurrentSections() {
		return getParser().getCurrentSections();
	}
	
	/**
	 * @see ParserInstance#setCurrentSections(List)
	 */
	@Deprecated
	public static void setCurrentSections(List<TriggerSection> currentSections) {
		getParser().setCurrentSections(currentSections);
	}
	
	/**
	 * @see ParserInstance#getCurrentSections(Class)
	 */
	@Deprecated
	public static List<SecLoop> getCurrentLoops() {
		return getParser().getCurrentSections(SecLoop.class);
	}

	/**
	 * Never use this method, it has no effect.
	 */
	@Deprecated
	public static void setCurrentLoops(List<SecLoop> currentLoops) { }
	
	/**
	 * @see ParserInstance#getCurrentEventName()
	 */
	@Nullable
	@Deprecated
	public static String getCurrentEventName() {
		return getParser().getCurrentEventName();
	}
	
	/**
	 * @see ParserInstance#setCurrentEvent(String, Class[])
	 */
	@SafeVarargs
	@Deprecated
	public static void setCurrentEvent(String name, @Nullable Class<? extends Event>... events) {
		getParser().setCurrentEvent(name, events);
	}
	
	/**
	 * @see ParserInstance#deleteCurrentEvent()
	 */
	@Deprecated
	public static void deleteCurrentEvent() {
		getParser().deleteCurrentEvent();
	}
	
	/**
	 * @see ParserInstance#isCurrentEvent(Class)
	 */
	@Deprecated
	public static boolean isCurrentEvent(@Nullable Class<? extends Event> event) {
		return getParser().isCurrentEvent(event);
	}
	
	/**
	 * @see ParserInstance#isCurrentEvent(Class[])
	 */
	@SafeVarargs
	@Deprecated
	public static boolean isCurrentEvent(Class<? extends Event>... events) {
		return getParser().isCurrentEvent(events);
	}
	
	/**
	 * @see ParserInstance#getCurrentEvents()
	 */
	@Nullable
	@Deprecated
	public static Class<? extends Event>[] getCurrentEvents() {
		return getParser().getCurrentEvents();
	}
	
}
