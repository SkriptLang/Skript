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
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.structures.StructOptions.OptionsData;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.TypeHints;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.OpenCloseable;
import ch.njol.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main class for loading, unloading and reloading scripts.
 */
public class ScriptLoader {

	public static final String DISABLED_SCRIPT_PREFIX = "-";
	public static final int DISABLED_SCRIPT_PREFIX_LENGTH = DISABLED_SCRIPT_PREFIX.length();
	
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

	// TODO We need to track scripts in the process of loading so that they may not be [re]loaded while they are already loading (for async loading)

	/**
	 * All loaded scripts.
	 */
	@SuppressWarnings("null")
	private static final Set<Script> loadedScripts = Collections.synchronizedSortedSet(new TreeSet<>(new Comparator<Script>() {
		@Override
		public int compare(Script s1, Script s2) {
			File f1 = s1.getConfig().getFile();
			File f2 = s2.getConfig().getFile();
			if (f1 == null || f2 == null)
				throw new IllegalArgumentException("Scripts will null config files cannot be sorted.");

			File f1Parent = f1.getParentFile();
			File f2Parent = f2.getParentFile();

			if (isSubDir(f1Parent, f2Parent))
				return -1;

			if (isSubDir(f2Parent, f1Parent))
				return 1;

			return f1.compareTo(f2);
		}

		private boolean isSubDir(File directory, File subDir) {
			for (File parentDir = directory.getParentFile(); parentDir != null; parentDir = parentDir.getParentFile()) {
				if (subDir.equals(parentDir))
					return true;
			}
			return false;
		}
	}));
	
	/**
	 * Filter for loaded scripts and folders.
	 */
	private static final FileFilter loadedScriptFilter =
		f -> f != null
			&& (f.isDirectory() && !f.getName().startsWith(".") || !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".sk"))
			&& !f.getName().startsWith(DISABLED_SCRIPT_PREFIX) && !f.isHidden();

	/**
	 * Searches through the loaded scripts to find the script loaded from the provided file.
	 * @param file The file containing the script to find. Must not be a directory.
	 * @return The script loaded from the provided file, or null if no script was found.
	 */
	@Nullable
	public static Script getScript(File file) {
		if (!file.isFile())
			throw new IllegalArgumentException("Something other than a file was provided.");
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
	public static Set<Script> getScripts(File directory) {
		if (!directory.isDirectory())
			throw new IllegalArgumentException("Something other than a directory was provided.");
		Set<Script> scripts = new HashSet<>();
		//noinspection ConstantConditions - If listFiles still manages to return null, we should probably let the exception print
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
			&& f.getName().startsWith(DISABLED_SCRIPT_PREFIX) && !f.isHidden();
	
	/**
	 * Reevaluates {@link #disabledScripts}.
	 * @param path the scripts folder to use for the reevaluation.
	 */
	static void updateDisabledScripts(Path path) {
		disabledScripts.clear();
		try (Stream<Path> files = Files.walk(path)) {
			files.map(Path::toFile)
				.filter(disabledScriptFilter::accept)
				.forEach(disabledScripts::add);
		} catch (Exception e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "An error occurred while trying to update the list of disabled scripts!");
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
	 * Script Loading Methods
	 */

	/**
	 * Loads the Script present at the file using {@link #loadScripts(List, OpenCloseable)},
	 * 	sending info/error messages when done.
	 * @param file The file to load. If this is a directory, all scripts within the directory and any subdirectories will be loaded.
	 * @param openCloseable An {@link OpenCloseable} that will be called before and after
	 *                         each individual script load (see {@link #makeFuture(Supplier, OpenCloseable)}).
	 */
	public static CompletableFuture<ScriptInfo> loadScripts(File file, OpenCloseable openCloseable) {
		return loadScripts(loadStructures(file), openCloseable);
	}

	/**
	 * Loads the Scripts present at the files using {@link #loadScripts(List, OpenCloseable)},
	 * 	sending info/error messages when done.
	 * @param files The files to load. If any file is a directory, all scripts within the directory and any subdirectories will be loaded.
	 * @param openCloseable An {@link OpenCloseable} that will be called before and after
	 *                         each individual script load (see {@link #makeFuture(Supplier, OpenCloseable)}).
	 */
	public static CompletableFuture<ScriptInfo> loadScripts(Set<File> files, OpenCloseable openCloseable) {
		return loadScripts(files.stream()
			.sorted()
			.map(ScriptLoader::loadStructures)
			.flatMap(List::stream)
			.collect(Collectors.toList()), openCloseable);
	}
	
	/**
	 * Loads the specified scripts.
	 *
	 * @param configs Configs representing scripts.
	 * @param openCloseable An {@link OpenCloseable} that will be called before and after
	 *  each individual script load (see {@link #makeFuture(Supplier, OpenCloseable)}).
	 * Note that this is also opened before the {@link Structure#preLoad()} stage
	 *  and closed after the {@link Structure#postLoad()} stage.
	 * @return Info on the loaded scripts.
	 */
	private static CompletableFuture<ScriptInfo> loadScripts(List<Config> configs, OpenCloseable openCloseable) {
		if (configs.isEmpty()) // Nothing to load
			return CompletableFuture.completedFuture(new ScriptInfo());

		Bukkit.getPluginManager().callEvent(new PreScriptLoadEvent(configs));
		
		ScriptInfo scriptInfo = new ScriptInfo();

		List<LoadingScriptInfo> scripts = new ArrayList<>();

		List<CompletableFuture<Void>> scriptInfoFutures = new ArrayList<>();
		for (Config config : configs) {
			if (config == null)
				throw new NullPointerException();
			
			CompletableFuture<Void> future = makeFuture(() -> {
				LoadingScriptInfo info = loadScript(config);
				scripts.add(info);
				scriptInfo.add(new ScriptInfo(1, info.structures.size()));
				return null;
			}, openCloseable);
			
			scriptInfoFutures.add(future);
		}
		
		return CompletableFuture.allOf(scriptInfoFutures.toArray(new CompletableFuture[0]))
			.thenApply(unused -> {
				// TODO in the future this won't work when parallel loading is fixed
				// It does now though so let's avoid calling getParser() a bunch.
				ParserInstance parser = getParser();

				try {
					openCloseable.open();

					// build sorted list
					// this nest of pairs is terrible, but we need to keep the reference to the modifiable structures list
					List<NonNullPair<LoadingScriptInfo, Structure>> pairs = scripts.stream()
							.flatMap(info -> { // Flatten each entry down to a stream of Script-Structure pairs
								return info.structures.stream()
										.map(structure -> new NonNullPair<>(info, structure));
							})
							.sorted(Comparator.comparing(pair -> pair.getSecond().getPriority()))
							.collect(Collectors.toCollection(ArrayList::new));

					// pre-loading
					pairs.removeIf(pair -> {
						LoadingScriptInfo loadingInfo = pair.getFirst();
						Structure structure = pair.getSecond();

						parser.setActive(loadingInfo.script);
						parser.setCurrentStructure(structure);
						parser.setNode(loadingInfo.nodeMap.get(structure));

						try {
							if (!structure.preLoad()) {
								loadingInfo.structures.remove(structure);
								return true;
							}
						} catch (Exception e) {
							//noinspection ThrowableNotThrown
							Skript.exception(e, "An error occurred while trying to preLoad a Structure.");
							loadingInfo.structures.remove(structure);
							return true;
						}
						return false;
					});
					parser.setInactive();

					// TODO in the future, Structure#load/Structure#postLoad should be split across multiple threads if parallel loading is enabled.
					// However, this is not possible right now as reworks in multiple areas will be needed.
					// For example, the "Commands" class still uses a static list for currentArguments that is cleared between loads.
					// Until these reworks happen, limiting main loading to asynchronous (not parallel) is the only choice we have.

					// loading
					pairs.removeIf(pair -> {
						LoadingScriptInfo loadingInfo = pair.getFirst();
						Structure structure = pair.getSecond();

						parser.setActive(loadingInfo.script);
						parser.setCurrentStructure(structure);
						parser.setNode(loadingInfo.nodeMap.get(structure));

						try {
							if (!structure.load()) {
								loadingInfo.structures.remove(structure);
								return true;
							}
						} catch (Exception e) {
							//noinspection ThrowableNotThrown
							Skript.exception(e, "An error occurred while trying to load a Structure.");
							loadingInfo.structures.remove(structure);
							return true;
						}
						return false;
					});
					parser.setInactive();

					// post-loading
					pairs.removeIf(pair -> {
						LoadingScriptInfo loadingInfo = pair.getFirst();
						Structure structure = pair.getSecond();

						parser.setActive(loadingInfo.script);
						parser.setCurrentStructure(structure);
						parser.setNode(loadingInfo.nodeMap.get(structure));

						try {
							if (!structure.postLoad()) {
								loadingInfo.structures.remove(structure);
								return true;
							}
						} catch (Exception e) {
							//noinspection ThrowableNotThrown
							Skript.exception(e, "An error occurred while trying to postLoad a Structure.");
							loadingInfo.structures.remove(structure);
							return true;
						}
						return false;
					});
					parser.setInactive();

					return scriptInfo;
				} catch (Exception e) {
					// Something went wrong, we need to make sure the exception is printed
					throw Skript.exception(e);
				} finally {
					parser.setInactive();

					openCloseable.close();
				}
			});
	}

	private static class LoadingScriptInfo {

		public final Script script;

		public final List<Structure> structures;

		public final Map<Structure, Node> nodeMap;

		public LoadingScriptInfo(Script script, List<Structure> structures, Map<Structure, Node> nodeMap) {
			this.script = script;
			this.structures = structures;
			this.nodeMap = nodeMap;
		}

	}

	/**
	 * Creates a script and loads the provided config into it.
	 * @param config The config to load into a script.
	 * @return A pair containing the script that was loaded and a modifiable version of the structures list.
	 */
	// Whenever you call this method, make sure to also call PreScriptLoadEvent
	private static LoadingScriptInfo loadScript(Config config) {
		if (config.getFile() == null)
			throw new IllegalArgumentException("A config must have a file to be loaded.");

		ParserInstance parser = getParser();
		Map<Structure, Node> nodeMap = new HashMap<>();
		List<Structure> structures = new ArrayList<>();
		Script script = new Script(config, structures);
		parser.setActive(script);

		try {
			if (SkriptConfig.keepConfigsLoaded.value())
				SkriptConfig.configs.add(config);
			
			try (CountingLogHandler ignored = new CountingLogHandler(SkriptLogger.SEVERE).start()) {
				for (Node node : config.getMainNode()) {
					if (!(node instanceof SimpleNode) && !(node instanceof SectionNode)) {
						// unlikely to occur, but just in case
						Skript.error("could not interpret line as a structure");
						continue;
					}

					String line = node.getKey();
					if (line == null)
						continue;
					line = replaceOptions(line); // replace options here before validation

					if (!SkriptParser.validateLine(line))
						continue;

					if (Skript.logVeryHigh() && !Skript.debug())
						Skript.info("loading trigger '" + line + "'");

					Structure structure = Structure.parse(line, node, "Can't understand this structure: " + line);

					if (structure == null)
						continue;

					structures.add(structure);
					nodeMap.put(structure, node);
				}
				
				if (Skript.logHigh()) {
					int count = structures.size();
					Skript.info("loaded " + count + " structure" + (count == 1 ? "" : "s") + " from '" + config.getFileName() + "'");
				}
			}
		} catch (Exception e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "Could not load " + config.getFileName());
		} finally {
			parser.setInactive();
		}
		
		// In always sync task, enable stuff
		Callable<Void> callable = () -> {
			// Remove the script from the disabled scripts list
			File file = config.getFile();
			assert file != null;
			File disabledFile = new File(file.getParentFile(), DISABLED_SCRIPT_PREFIX + file.getName());
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

		return new LoadingScriptInfo(script, structures, nodeMap);
	}

	/*
	 * Script Structure Loading Methods
	 */
	
	/**
	 * Creates a script structure for every file contained within the provided directory.
	 * If a directory is not actually provided, the file itself will be used.
	 * @param directory The directory to create structures from.
	 * @see ScriptLoader#loadStructure(File)
	 * @return A list of all successfully loaded structures.
	 */
	private static List<Config> loadStructures(File directory) {
		if (!directory.isDirectory()) {
			Config config = loadStructure(directory);
			return config != null ? Collections.singletonList(config) : Collections.emptyList();
		}

		try {
			directory = directory.getCanonicalFile();
		} catch (IOException e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "An exception occurred while trying to get the canonical file of: " + directory);
			return new ArrayList<>();
		}
		
		File[] files = directory.listFiles(loadedScriptFilter);
		assert files != null;
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
	 * Creates a script structure from the provided file.
	 * This must be done before actually loading a script.
	 * @param file The script to load the structure of.
	 * @return The loaded structure or null if an error occurred.
	 */
	@Nullable
	private static Config loadStructure(File file) {
		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "An exception occurred while trying to get the canonical file of: " + file);
			return null;
		}

		if (!file.exists()) { // If file does not exist...
			Script script = getScript(file);
			if (script != null)
				unloadScript(script); // ... it might be good idea to unload it now
			return null;
		}
		
		try {
			String name = Skript.getInstance().getDataFolder().toPath().toAbsolutePath()
					.resolve(Skript.SCRIPTSFOLDER).relativize(file.toPath().toAbsolutePath()).toString();
			return loadStructure(Files.newInputStream(file.toPath()), name);
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
	private static Config loadStructure(InputStream source, String name) {
		try {
			return new Config(
				source,
				name,
				Skript.getInstance().getDataFolder().toPath().resolve(Skript.SCRIPTSFOLDER).resolve(name).toFile().getCanonicalFile(),
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
	 * Script Unloading Methods
	 */

	/**
	 * Unloads all scripts present in the provided collection.
	 * @param scripts The scripts to unload.
	 * @return Combined statistics for the unloaded scripts.
	 *         This data is calculated by using {@link ScriptInfo#add(ScriptInfo)}.
	 */
	public static ScriptInfo unloadScripts(Set<Script> scripts) {
		// ensure unloaded scripts are not being unloaded
		for (Script script : scripts) {
			if (!loadedScripts.contains(script))
				throw new SkriptAPIException("The script at '" + script.getConfig().getPath() + "' is not loaded!");
			if (script.getConfig().getFile() == null)
				throw new IllegalArgumentException("A script must have a file to be unloaded.");
		}

		ParserInstance parser = getParser();

		// initial unload stage
		for (Script script : scripts) {
			parser.setActive(script);
			for (Structure structure : script.getStructures())
				structure.unload();
		}

		parser.setInactive();

		// finish unloading + data collection
		ScriptInfo info = new ScriptInfo();
		for (Script script : scripts) {
			List<Structure> structures = script.getStructures();

			info.files++;
			info.structures += structures.size();

			parser.setActive(script);
			for (Structure structure : structures)
				structure.postUnload();
			parser.setInactive();

			script.clearData();
			loadedScripts.remove(script); // We just unloaded it, so...
			File scriptFile = script.getConfig().getFile();
			assert scriptFile != null;
			disabledScripts.add(new File(scriptFile.getParentFile(), DISABLED_SCRIPT_PREFIX + scriptFile.getName()));
		}

		return info;
	}
	
	/**
	 * Unloads the provided script.
	 * @param script The script to unload.
	 * @return Statistics for the unloaded script.
	 */
	public static ScriptInfo unloadScript(Script script) {
		return unloadScripts(Collections.singleton(script));
	}
	
	/*
	 * Script Reloading Methods
	 */

	/**
	 * Reloads a single Script.
	 * @param script The Script to reload.
	 * @return Info on the loaded Script.
	 */
	public static CompletableFuture<ScriptInfo> reloadScript(Script script, OpenCloseable openCloseable) {
		return reloadScripts(Collections.singleton(script), openCloseable);
	}

	/**
	 * Reloads all provided Scripts.
	 * @param scripts The Scripts to reload.
	 * @param openCloseable An {@link OpenCloseable} that will be called before and after
	 *                         each individual Script load (see {@link #makeFuture(Supplier, OpenCloseable)}).
	 * @return Info on the loaded Scripts.
	 */
	public static CompletableFuture<ScriptInfo> reloadScripts(Set<Script> scripts, OpenCloseable openCloseable) {
		unloadScripts(scripts);

		List<Config> configs = new ArrayList<>();
		for (Script script : scripts) {
			//noinspection ConstantConditions - getFile should never return null
			Config config = loadStructure(script.getConfig().getFile());
			if (config == null)
				return CompletableFuture.completedFuture(new ScriptInfo());
			configs.add(config);
		}

		return loadScripts(configs, openCloseable);
	}
	
	/*
	 * Code Loading Methods
	 */

	/**
	 * Replaces options in a string.
	 * Options are obtained from a {@link Script}'s {@link OptionsData}.
	 * Example: <code>script.getData(OptionsData.class)</code>
	 */
	// TODO this system should eventually be replaced with a more generalized "node processing" system
	public static String replaceOptions(String string) {
		ParserInstance parser = getParser();
		if (!parser.isActive()) // getCurrentScript() is not safe to use
			return string;
		OptionsData optionsData = parser.getCurrentScript().getData(OptionsData.class);
		if (optionsData == null)
			return string;
		return optionsData.replaceOptions(string);
	}
	
	/**
	 * Loads a section by converting it to {@link TriggerItem}s.
	 */
	public static ArrayList<TriggerItem> loadItems(SectionNode node) {
		ParserInstance parser = getParser();

		if (Skript.debug())
			parser.setIndentation(parser.getIndentation() + "    ");
		
		ArrayList<TriggerItem> items = new ArrayList<>();

		for (Node subNode : node) {
			parser.setNode(subNode);

			String subNodeKey = subNode.getKey();
			if (subNodeKey == null)
				throw new IllegalArgumentException("Encountered node with null key: '" + subNode + "'");
			String expr = replaceOptions(subNodeKey);
			if (!SkriptParser.validateLine(expr))
				continue;

			if (subNode instanceof SimpleNode) {
				long start = System.currentTimeMillis();
				Statement stmt = Statement.parse(expr, items, "Can't understand this condition/effect: " + expr);
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

				if (Skript.debug() || subNode.debug())
					Skript.debug(SkriptColor.replaceColorChar(parser.getIndentation() + stmt.toString(null, true)));

				items.add(stmt);
			} else if (subNode instanceof SectionNode) {
				TypeHints.enterScope(); // Begin conditional type hints

				Section section = Section.parse(expr, "Can't understand this section: " + expr, (SectionNode) subNode, items);
				if (section == null)
					continue;

				if (Skript.debug() || subNode.debug())
					Skript.debug(SkriptColor.replaceColorChar(parser.getIndentation() + section.toString(null, true)));

				items.add(section);

				// Destroy these conditional type hints
				TypeHints.exitScope();
			}
		}
		
		for (int i = 0; i < items.size() - 1; i++)
			items.get(i).setNext(items.get(i + 1));

		parser.setNode(node);
		
		if (Skript.debug())
			parser.setIndentation(parser.getIndentation().substring(0, parser.getIndentation().length() - 4));
		
		return items;
	}

	/*
	 * Other Utility Methods
	 */

	/**
	 * @return An unmodifiable set containing a snapshot of the currently loaded scripts.
	 * Any changes to loaded scripts will not be reflected in the returned set.
	 */
	public static Set<Script> getLoadedScripts() {
		return Collections.unmodifiableSet(new HashSet<>(loadedScripts));
	}

	/**
	 * @return An unmodifiable set containing a snapshot of the currently disabled scripts.
	 * Any changes to disabled scripts will not be reflected in the returned set.
	 */
	public static Set<File> getDisabledScripts() {
		return Collections.unmodifiableSet(new HashSet<>(disabledScripts));
	}

	/**
	 * @return A FileFilter defining the naming conditions of a loaded script.
	 */
	public static FileFilter getLoadedScriptsFilter() {
		return loadedScriptFilter;
	}

	/**
	 * @return A FileFilter defining the naming conditions of a disabled script.
	 */
	public static FileFilter getDisabledScriptsFilter() {
		return disabledScriptFilter;
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
	 * Unloads the provided script.
	 * @param scriptFile The file representing the script to unload.
	 * @return Statistics for the unloaded script.
	 * @deprecated Use {@link #unloadScript(Script)}.
	 */
	@Deprecated
	public static ScriptInfo unloadScript(File scriptFile) {
		Script script = getScript(scriptFile);
		if (script != null)
			return unloadScript(script);
		return new ScriptInfo();
	}

	/**
	 * Unloads all scripts present in the provided folder.
	 * @param folder The folder containing scripts to unload.
	 * @return Combined statistics for the unloaded scripts.
	 *         This data is calculated by using {@link ScriptInfo#add(ScriptInfo)}.
	 * @deprecated Use {@link #unloadScripts(Set)}.
	 */
	@Deprecated
	private static ScriptInfo unloadScripts(File folder) {
		return unloadScripts(getScripts(folder));
	}

	/**
	 * Reloads a single script.
	 * @param scriptFile The file representing the script to reload.
	 * @return Future of statistics of the newly loaded script.
	 * @deprecated Use {@link #reloadScript(Script, OpenCloseable)}.
	 */
	@Deprecated
	public static CompletableFuture<ScriptInfo> reloadScript(File scriptFile, OpenCloseable openCloseable) {
		unloadScript(scriptFile);
		return loadScripts(scriptFile, openCloseable);
	}

	/**
	 * Reloads all scripts in the given folder and its subfolders.
	 * @param folder A folder.
	 * @return Future of statistics of newly loaded scripts.
	 * @deprecated Use {@link #reloadScripts}.
	 */
	@Deprecated
	public static CompletableFuture<ScriptInfo> reloadScripts(File folder, OpenCloseable openCloseable) {
		unloadScripts(folder);
		return loadScripts(folder, openCloseable);
	}

	/**
	 * @deprecated Use <b>{@link #getLoadedScripts()}.size()</b>.
	 */
	@Deprecated
	public static int loadedScripts() {
		return getLoadedScripts().size();
	}

	/**
	 * @deprecated Use <b>{@link #getLoadedScripts()}</b> and <b>{@link Script#getStructures()}.size()</b>.
	 * Please note that a Structure may have multiple triggers, and this is only an estimate.
	 */
	@Deprecated
	public static int loadedTriggers() {
		int loaded = 0;
		for (Script script : getLoadedScripts())
			loaded += script.getStructures().size();
		return loaded;
	}

	/**
	 * @deprecated Use {@link #loadScripts(File, OpenCloseable)}
	 */
	@Deprecated
	static void loadScripts() {
		unloadScripts(getLoadedScripts());
		loadScripts(Skript.getInstance().getScriptsFolder(), OpenCloseable.EMPTY).join();
	}

	/**
	 * @deprecated Callers should not be using configs. Use {@link #loadScripts(Set, OpenCloseable)}.
	 */
	@Deprecated
	public static ScriptInfo loadScripts(List<Config> configs) {
		return loadScripts(configs, OpenCloseable.EMPTY).join();
	}

	/**
	 * @deprecated Callers should not be using configs. Use {@link #loadScripts(Set, OpenCloseable)}.
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
	 * @deprecated Callers should not be using configs. Use {@link #loadScripts(Set, OpenCloseable)}.
	 */
	@Deprecated
	public static ScriptInfo loadScripts(Config... configs) {
		return loadScripts(Arrays.asList(configs), OpenCloseable.EMPTY).join();
	}

	/**
	 * @deprecated Use {@link #reloadScript(Script, OpenCloseable)}.
	 */
	@Deprecated
	public static ScriptInfo reloadScript(File script) {
		return reloadScript(script, OpenCloseable.EMPTY).join();
	}

	/**
	 * @deprecated Use {@link #reloadScripts(Set, OpenCloseable)}.
	 */
	@Deprecated
	public static ScriptInfo reloadScripts(File folder) {
		return reloadScripts(folder, OpenCloseable.EMPTY).join();
	}

	/**
	 * @deprecated Use {@link ParserInstance#getHasDelayBefore()}.
	 */
	@Deprecated
	public static Kleenean getHasDelayBefore() {
		return getParser().getHasDelayBefore();
	}

	/**
	 * @deprecated Use {@link ParserInstance#setHasDelayBefore(Kleenean)}.
	 */
	@Deprecated
	public static void setHasDelayBefore(Kleenean hasDelayBefore) {
		getParser().setHasDelayBefore(hasDelayBefore);
	}

	/**
	 * @deprecated Use {@link ParserInstance#getCurrentScript()}.
	 */
	@Nullable
	@Deprecated
	public static Config getCurrentScript() {
		ParserInstance parser = getParser();
		return parser.isActive() ? parser.getCurrentScript().getConfig() : null;
	}

	/**
	 * @deprecated Addons should no longer be modifying this.
	 */
	@Deprecated
	public static void setCurrentScript(@Nullable Config currentScript) {
		getParser().setCurrentScript(currentScript);
	}

	/**
	 * @deprecated Use {@link ParserInstance#getCurrentSections()}.
	 */
	@Deprecated
	public static List<TriggerSection> getCurrentSections() {
		return getParser().getCurrentSections();
	}

	/**
	 * @deprecated Use {@link ParserInstance#setCurrentSections(List)}.
	 */
	@Deprecated
	public static void setCurrentSections(List<TriggerSection> currentSections) {
		getParser().setCurrentSections(currentSections);
	}

	/**
	 * @deprecated Use {@link ParserInstance#getCurrentSections(Class)}.
	 */
	@Deprecated
	public static List<SecLoop> getCurrentLoops() {
		return getParser().getCurrentSections(SecLoop.class);
	}

	/**
	 * @deprecated Never use this method, it has no effect.
	 */
	@Deprecated
	public static void setCurrentLoops(List<SecLoop> currentLoops) { }

	/**
	 * @deprecated Use {@link ParserInstance#getCurrentEventName()}.
	 */
	@Nullable
	@Deprecated
	public static String getCurrentEventName() {
		return getParser().getCurrentEventName();
	}

	/**
	 * @deprecated Use {@link ParserInstance#setCurrentEvent(String, Class[])}.
	 */
	@SafeVarargs
	@Deprecated
	public static void setCurrentEvent(String name, @Nullable Class<? extends Event>... events) {
		getParser().setCurrentEvent(name, events);
	}

	/**
	 * @deprecated Use {@link ParserInstance#deleteCurrentEvent()}.
	 */
	@Deprecated
	public static void deleteCurrentEvent() {
		getParser().deleteCurrentEvent();
	}

	/**
	 * @deprecated Use {@link ParserInstance#isCurrentEvent(Class)}
	 */
	@Deprecated
	public static boolean isCurrentEvent(@Nullable Class<? extends Event> event) {
		return getParser().isCurrentEvent(event);
	}

	/**
	 * @deprecated Use {@link ParserInstance#isCurrentEvent(Class[])}.
	 */
	@SafeVarargs
	@Deprecated
	public static boolean isCurrentEvent(Class<? extends Event>... events) {
		return getParser().isCurrentEvent(events);
	}

	/**
	 * @deprecated Use {@link ParserInstance#getCurrentEvents()}.
	 */
	@Nullable
	@Deprecated
	public static Class<? extends Event>[] getCurrentEvents() {
		return getParser().getCurrentEvents();
	}

	/**
	 * @deprecated This method has no functionality, it just returns its input.
	 */
	@Deprecated
	public static Config loadStructure(Config config) {
		return config;
	}

}
