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

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import ch.njol.skript.lang.Script;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.structure.Structure;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.structures.StructOptions;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Task;
import ch.njol.skript.variables.TypeHints;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * The main class for loading, unloading and reloading scripts.
 */
public class ScriptLoader {
	
	private static final Message
		m_no_errors = new Message("skript.no errors"),
		m_no_scripts = new Message("skript.no scripts");
	private static final PluralizingArgsMessage m_scripts_loaded = new PluralizingArgsMessage("skript.scripts loaded");
	
	/**
	 * Disables all scripts by unloading all structures.
	 */
	static void disableScripts() {
		unloadScripts(new HashSet<>(loadedScripts));
	}
	
	/**
	 * A class for keeping track of the general content of a script:
	 * <ul>
	 *     <li>The amount of files</li>
	 *     <li>The amount of structures</li>
	 * </ul>
	 */
	public static class ScriptInfo {
		public int files, structures;

		public ScriptInfo() {

		}
		
		public ScriptInfo(int numFiles, int numStructures) {
			files = numFiles;
			structures = numStructures;
		}
		
		/**
		 * Copy constructor.
		 * @param other ScriptInfo to copy from
		 */
		public ScriptInfo(ScriptInfo other) {
			files = other.files;
			structures = other.structures;
		}
		
		public void add(ScriptInfo other) {
			files += other.files;
			structures += other.structures;
		}
		
		public void subtract(ScriptInfo other) {
			files -= other.files;
			structures -= other.structures;
		}
		
		@Override
		public String toString() {
			return "ScriptInfo{files=" + files + ",structures=" + structures + "}";
		}
	}
	
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
	 * All loaded scripts.
	 */
	@SuppressWarnings("null")
	private static final Set<Script> loadedScripts = Collections.synchronizedSet(new HashSet<>());
	
	/**
	 * Filter for loaded scripts and folders.
	 */
	private static final FileFilter loadedScriptFilter =
		f -> f != null
			&& (f.isDirectory() && !f.getName().startsWith(".") || !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".sk"))
			&& !f.getName().startsWith("-") && !f.isHidden();

	/**
	 * Searches through the loaded scripts to find the script loaded from the provided file.
	 * @param file The file containing the script to find. Must not be a directory.
	 * @return The script loaded from the provided file, or null if no script was found.
	 */
	@Nullable
	public static Script getScript(File file) {
		if (file.isDirectory())
			throw new IllegalArgumentException("A directory was provided.");
		for (Script script : loadedScripts) {
			if (file.equals(script.getConfig().getFile()))
				return script;
		}
		return null;
	}

	/**
	 * Searches through the loaded scripts to find all scripts loaded from the files contained within the provided directory.
	 * @param directory The directory containing scripts to find.
	 * @return The scripts loaded from the files of the provided directory.
	 * 	Empty if no scripts were found.
	 */
	public static List<Script> getScripts(File directory) {
		List<Script> scripts = new ArrayList<>();
		for (File file : directory.listFiles(loadedScriptFilter)) {
			if (file.isDirectory()) {
				scripts.addAll(getScripts(file));
			} else {
				Script script = getScript(file);
				if (script != null)
					scripts.add(script);
			}
		}
		return scripts;
	}

	/**
	 * All disabled script files.
	 */
	private static final Set<File> disabledScripts = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Filter for disabled scripts and folders.
	 */
	private static final FileFilter disabledScriptFilter =
		f -> f != null
			&& (f.isDirectory() && !f.getName().startsWith(".") || !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".sk"))
			&& f.getName().startsWith("-") && !f.isHidden();
	
	/**
	 * Reevaluates {@link #disabledScripts}.
	 * @param path the scripts folder to use for the reevaluation.
	 */
	private static void updateDisabledScripts(Path path) {
		disabledScripts.clear();
		try {
			// TODO handle AccessDeniedException
			Files.walk(path)
				.map(Path::toFile)
				.filter(disabledScriptFilter::accept)
				.forEach(disabledScripts::add);
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

		List<Config> configs;
		
		CountingLogHandler logHandler = new CountingLogHandler(Level.SEVERE).start();
		try {
			configs = loadStructures(scriptsFolder);
		} finally {
			logHandler.stop();
		}
		
		return loadScripts(configs, OpenCloseable.combine(openCloseable, logHandler))
			.thenAccept(scriptInfo -> {
				try {
					if (logHandler.getCount() == 0)
						Skript.info(m_no_errors.toString());
					if (scriptInfo.files == 0)
						Skript.warning(m_no_scripts.toString());
					if (Skript.logNormal() && scriptInfo.files > 0)
						Skript.info(m_scripts_loaded.toString(
							scriptInfo.files,
							scriptInfo.structures,
							start.difference(new Date())
						));
				} catch (Exception e) {
					// Something went wrong, we need to make sure the exception is printed
					throw Skript.exception(e);
				}
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
		Bukkit.getPluginManager().callEvent(new PreScriptLoadEvent(configs));
		
		ScriptInfo scriptInfo = new ScriptInfo();

		List<Script> scripts = new ArrayList<>();

		List<CompletableFuture<Void>> scriptInfoFutures = new ArrayList<>();
		for (Config config : configs) {
			if (config == null)
				throw new NullPointerException();
			
			CompletableFuture<Void> future = makeFuture(() -> {
				Script script = new Script(config);
				ScriptInfo info = loadScript(script);
				scripts.add(script);
				scriptInfo.add(info);
				return null;
			}, openCloseable);
			
			scriptInfoFutures.add(future);
		}
		
		return CompletableFuture.allOf(scriptInfoFutures.toArray(new CompletableFuture[0]))
			.thenApply(unused -> {
				try {
					openCloseable.open();

					scripts.stream()
						.flatMap(script -> { // Flatten each entry down to a stream of Config-Structure pairs
							return script.getStructures().stream()
								.map(structure -> new NonNullPair<>(script, structure));
						})
						.sorted(Comparator.comparing(pair -> pair.getSecond().getPriority()))
						.forEach(pair -> {
							Script script = pair.getFirst();
							if (getParser().getCurrentScript() != script)
								getParser().setCurrentScript(script);
							pair.getSecond().preLoad();
						});

					for (Script script : scripts) {
						getParser().setCurrentScript(script);
						for (Structure structure : script.getStructures())
							structure.load();
					}

					for (Script script : scripts) {
						getParser().setCurrentScript(script);
						for (Structure structure : script.getStructures())
							structure.postLoad();
					}

					return scriptInfo;
				} catch (Exception e) {
					// Something went wrong, we need to make sure the exception is printed
					throw Skript.exception(e);
				} finally {
					SkriptLogger.setNode(null);

					openCloseable.close();
				}
			});
	}

	/**
	 * Loads one script. Only for internal use, as this doesn't register/update event handlers.
	 * @param script The script to be loaded.
	 * @return Statistics for the script loaded.
	 */
	// Whenever you call this method, make sure to also call PreScriptLoadEvent
	private static ScriptInfo loadScript(@Nullable Script script) {
		if (script == null) { // Something bad happened, hopefully got logged to console
			return new ScriptInfo();
		}

		// Track what is loaded
		ScriptInfo scriptInfo = new ScriptInfo();
		getParser().setScriptInfo(scriptInfo);
		scriptInfo.files = 1; // Loading one script

		List<Structure> structures = getParser().getLoadedStructures();
		structures.clear();

		Config config = script.getConfig();

		try {
			if (SkriptConfig.keepConfigsLoaded.value())
				SkriptConfig.configs.add(config);

			getParser().setCurrentScript(script);
			
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

					if (!SkriptParser.validateLine(event))
						continue;

					if (Skript.logVeryHigh() && !Skript.debug())
						Skript.info("loading trigger '" + event + "'");

					event = replaceOptions(event);

					Structure structure = Structure.parse(event, node, "can't understand this event: '" + node.getKey() + "'");

					if (structure == null)
						continue;

					script.getStructures().add(structure);
					structures.add(structure);

					scriptInfo.structures++;
				}
				
				if (Skript.logHigh())
					Skript.info("loaded " + scriptInfo.structures + " structure" + (scriptInfo.structures == 1 ? "" : "s") + " from '" + config.getFileName() + "'");
				
				getParser().setCurrentScript(null);
			}
		} catch (Exception e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "Could not load " + config.getFileName());
		} finally {
			SkriptLogger.setNode(null);
		}
		
		// In always sync task, enable stuff
		Callable<Void> callable = () -> {
			// Remove the script from the disabled scripts list
			File file = config.getFile();
			assert file != null;
			File disabledFile = new File(file.getParentFile(), "-" + file.getName());
			disabledScripts.remove(disabledFile);
			
			// Add to loaded files to use for future reloads
			loadedScripts.add(script);
			
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
	 * Creates a script structure for every provided file.
	 * @param files The files to create structures from.
	 * @see ScriptLoader#loadStructure(File)
	 * @return A list of all successfully loaded structures.
	 */
	public static List<Config> loadStructures(File[] files) {
		Arrays.sort(files);
		
		List<Config> loadedFiles = new ArrayList<>(files.length);
		for (File file : files) {
			assert file != null : Arrays.toString(files);
			Config config = loadStructure(file);
			if (config != null)
				loadedFiles.add(config);
		}
		
		return loadedFiles;
	}
	
	/**
	 * Creates a script structure for every file contained within the provided directory.
	 * If a directory is not actually provided, the file itself will be used.
	 * @param directory The directory to create structures from.
	 * @see ScriptLoader#loadStructure(File)
	 * @return A list of all successfully loaded structures.
	 */
	public static List<Config> loadStructures(File directory) {
		if (!directory.isDirectory()) {
			Config config = loadStructure(directory);
			return config != null ? Collections.singletonList(config) : Collections.emptyList();
		}
		
		File[] files = directory.listFiles(loadedScriptFilter);
		Arrays.sort(files);
		
		List<Config> loadedFiles = new ArrayList<>(files.length);
		for (File file : files) {
			if (file.isDirectory()) {
				loadedFiles.addAll(loadStructures(file));
			} else {
				Config cfg = loadStructure(file);
				if (cfg != null)
					loadedFiles.add(cfg);
			}
		}
		return loadedFiles;
	}
	
	/**
	 * Creates a script structure from the provided file.
	 * This must be done before actually loading a script.
	 * @param file The script to load the structure of.
	 * @return The loaded structure or null if an error occurred.
	 */
	@Nullable
	@SuppressWarnings("resource") // Stream is closed in Config constructor called in loadStructure
	public static Config loadStructure(File file) {
		if (!file.exists()) { // If file does not exist...
			Script script = getScript(file);
			if (script != null)
				unloadScript(script); // ... it might be good idea to unload it now
			return null;
		}
		
		try {
			String name = Skript.getInstance().getDataFolder().toPath().toAbsolutePath()
					.resolve(Skript.SCRIPTSFOLDER).relativize(file.toPath().toAbsolutePath()).toString();
			assert name != null;
			return loadStructure(new FileInputStream(file), name);
		} catch (IOException e) {
			Skript.error("Could not load " + file.getName() + ": " + ExceptionUtils.toString(e));
		}
		
		return null;
	}
	
	/**
	 * Creates a script structure from the provided source.
	 * This must be done before actually loading a script.
	 * @param source Source input stream.
	 * @param name Name of source "file".
	 * @return The loaded structure or null if an error occurred.
	 */
	@Nullable
	public static Config loadStructure(InputStream source, String name) {
		try {
			return new Config(
				source,
				name,
				Skript.getInstance().getDataFolder().toPath().resolve(Skript.SCRIPTSFOLDER).resolve(name).toFile(),
				true,
				false,
				":"
			);
		} catch (IOException e) {
			Skript.error("Could not load " + name + ": " + ExceptionUtils.toString(e));
		}
		
		return null;
	}

	/*
	 * Script unloading methods
	 */

	/**
	 * Unloads all scripts present in the provided folder.
	 * @param folder The folder containing scripts to unload.
	 * @return Combined statistics for the unloaded scripts.
	 *         This data is calculated by using {@link ScriptInfo#add(ScriptInfo)}.
	 */
	private static ScriptInfo unloadScripts(File folder) {
		return unloadScripts(getScripts(folder));
	}

	/**
	 * Unloads all scripts present in the provided collection.
	 * @param scripts The scripts to unload.
	 * @return Combined statistics for the unloaded scripts.
	 *         This data is calculated by using {@link ScriptInfo#add(ScriptInfo)}.
	 */
	public static ScriptInfo unloadScripts(Collection<Script> scripts) {
		ParserInstance parser = getParser();
		ScriptInfo info = new ScriptInfo();

		scripts = new ArrayList<>(scripts); // Don't modify the list we were provided with
		scripts.removeIf(script -> !loadedScripts.contains(script));

		for (Script script : scripts) {
			parser.setCurrentScript(script);
			for (Structure structure : script.getStructures())
				structure.unload();
			parser.setCurrentScript(null);
		}

		for (Script script : scripts) {
			List<Structure> structures = script.getStructures();

			info.files++;
			info.structures += structures.size();

			parser.setCurrentScript(script);
			for (Structure structure : script.getStructures())
				structure.postUnload();
			structures.clear();
			parser.setCurrentScript(null);

			loadedScripts.remove(script); // We just unloaded it, so...
			File scriptFile = script.getConfig().getFile();
			assert scriptFile != null;
			disabledScripts.add(new File(scriptFile.getParentFile(), "-" + scriptFile.getName()));

			// If unloading, our caller will do this immediately after we return
			// However, if reloading, new version of this script is first loaded

			//noinspection ConstantConditions - getPath should never return null
			String name = Skript.getInstance().getDataFolder().toPath().toAbsolutePath()
				.resolve(Skript.SCRIPTSFOLDER).relativize(script.getConfig().getPath().toAbsolutePath()).toString();
			assert name != null;
		}

		return info;
	}

	/**
	 * Unloads the provided script.
	 * @param scriptFile The file representing the script to unload.
	 * @return Statistics for the unloaded script.
	 */
	public static ScriptInfo unloadScript(File scriptFile) {
		Script script = getScript(scriptFile);
		if (script != null)
			return unloadScript(script);
		return new ScriptInfo();
	}
	
	/**
	 * Unloads the provided script.
	 * @param script The script to unload.
	 * @return Statistics for the unloaded script.
	 */
	public static ScriptInfo unloadScript(Script script) {
		return unloadScripts(Collections.singletonList(script));
	}
	
	/*
	 * Script reloading methods
	 */

	/**
	 * Reloads a single script.
	 * @param scriptFile The file representing the script to reload.
	 * @return Future of statistics of the newly loaded script.
	 */
	public static CompletableFuture<ScriptInfo> reloadScript(File scriptFile, OpenCloseable openCloseable) {
		Script script = getScript(scriptFile);
		if (script == null)
			return CompletableFuture.completedFuture(new ScriptInfo());
		return reloadScript(script, openCloseable);
	}

	/**
	 * Reloads a single script.
	 * @param script The script to reload.
	 * @return Future of statistics of the newly loaded script.
	 */
	public static CompletableFuture<ScriptInfo> reloadScript(Script script, OpenCloseable openCloseable) {
		unloadScript(script);
		//noinspection ConstantConditions - getFile should never return null
		Config config = loadStructure(script.getConfig().getFile());
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
		unloadScripts(folder);
		List<Config> configs = loadStructures(folder);
		return loadScripts(configs, openCloseable);
	}
	
	/*
	 * Code loading methods
	 */

	/**
	 * Replaces options in a string.
	 * Options are gotten from {@link ch.njol.skript.structures.StructOptions#getOptions(Script)}.
	 */
	// TODO this system should eventually be replaced with a more generalized "node processing" system
	public static String replaceOptions(String s) {
		Script script = getParser().getCurrentScript();
		if (script == null)
			return s;
		return StructOptions.replaceOptions(script, s);
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

				Statement stmt = Statement.parse(expr, "Can't understand this condition/effect: " + expr);
				if (stmt == null)
					continue;

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
	
	/*
	 * Loaded script statistics
	 */

	public static Collection<Script> getLoadedScripts() {
		return Collections.unmodifiableCollection(loadedScripts);
	}

	public static Collection<File> getDisabledScripts() {
		return Collections.unmodifiableCollection(disabledScripts);
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
	 * @deprecated No longer supported.
	 */
	@Deprecated
	public static int loadedScripts() {
		return 0;
	}

	/**
	 * @deprecated No longer supported.
	 */
	@Deprecated
	public static int loadedTriggers() {
		return 0;
	}

	/**
	 * @see #loadScripts(OpenCloseable)
	 */
	@Deprecated
	static void loadScripts() {
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
		Script script = getParser().getCurrentScript();
		return script != null ? script.getConfig() : null;
	}

	/**
	 * @see ParserInstance#setCurrentScript(Script)
	 */
	@Deprecated
	public static void setCurrentScript(@Nullable Config currentScript) {
		//noinspection ConstantConditions - getFile should never return null
		getParser().setCurrentScript(currentScript != null ? getScript(currentScript.getFile()) : null);
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

	/**
	 * This method has no functionality, it just returns its input.
	 */
	@Deprecated
	public static Config loadStructure(Config config) {
		return config;
	}

}
