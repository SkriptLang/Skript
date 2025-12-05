package ch.njol.skript;

import ch.njol.skript.examples.ExampleScript;
import ch.njol.skript.examples.ExampleScriptManager;
import ch.njol.skript.examples.ExampleScriptProvider;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Registry;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for Skript addons. Use {@link Skript#registerAddon(JavaPlugin)} to create a SkriptAddon instance for your plugin.
 */
public final class SkriptAddon implements org.skriptlang.skript.addon.SkriptAddon {

	public final JavaPlugin plugin;
	public final Version version;
	private final String name;

	private final org.skriptlang.skript.addon.SkriptAddon addon;

	/**
	 * Package-private constructor. Use {@link Skript#registerAddon(JavaPlugin)} to get a SkriptAddon for your plugin.
	 */
	SkriptAddon(JavaPlugin plugin) {
		this(plugin, Skript.instance().registerAddon(plugin.getClass(), plugin.getName()));
	}

	SkriptAddon(JavaPlugin plugin, org.skriptlang.skript.addon.SkriptAddon addon) {
		this.addon = addon;
		this.plugin = plugin;
		this.name = plugin.getName();
		Version version;
		try {
			version = new Version(plugin.getDescription().getVersion());
		} catch (IllegalArgumentException e) {
			final Matcher m = Pattern.compile("(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?").matcher(plugin.getDescription().getVersion());
			if (!m.find())
				throw new IllegalArgumentException("The version of the plugin " + name + " does not contain any numbers: " + plugin.getDescription().getVersion());
			version = new Version(Utils.parseInt(m.group(1)), m.group(2) == null ? 0 : Utils.parseInt(m.group(2)), m.group(3) == null ? 0 : Utils.parseInt(m.group(3)));
			Skript.warning("The plugin " + name + " uses a non-standard version syntax: '" + plugin.getDescription().getVersion() + "'. Skript will use " + version + " instead.");
		}
		this.version = version;
	}

	@Override
	public final String toString() {
		return getName();
	}

	public String getName() {
		return name;
	}

	/**
	 * Loads classes of the plugin by package. Useful for registering many syntax elements like Skript does it.
	 * 
	 * @param basePackage The base package to add to all sub packages, e.g. <tt>"ch.njol.skript"</tt>.
	 * @param subPackages Which subpackages of the base package should be loaded, e.g. <tt>"expressions", "conditions", "effects"</tt>. Subpackages of these packages will be loaded
	 *            as well. Use an empty array to load all subpackages of the base package.
	 * @throws IOException If some error occurred attempting to read the plugin's jar file.
	 * @return This SkriptAddon
	 */
	public SkriptAddon loadClasses(String basePackage, String... subPackages) throws IOException {
		Utils.getClasses(plugin, basePackage, subPackages);
		return this;
	}

	/**
	 * Makes Skript load language files from the specified directory, e.g. "lang" or "skript lang" if you have a lang folder yourself. Localised files will be read from the
	 * plugin's jar and the plugin's data folder, but the default English file is only taken from the jar and <b>must</b> exist!
	 * 
	 * @param directory Directory name
	 * @return This SkriptAddon
	 */
	public SkriptAddon setLanguageFileDirectory(String directory) {
		localizer().setSourceDirectories(directory, plugin.getDataFolder().getAbsolutePath() + directory);
		return this;
	}

	@Nullable
	public String getLanguageFileDirectory() {
		return localizer().languageFileDirectory();
	}

	/**
	 * Registers example scripts with Skript for this addon.
	 *
	 * @param scripts Example scripts to install
	 * @throws IOException If an I/O error occurs while installing the scripts
	 */
	public void registerExampleScripts(ExampleScript... scripts) throws IOException {
		ExampleScriptManager manager = Skript.getExampleManager();
		if (manager == null)
			throw new IllegalStateException("Example script manager is not initialized");

		manager.installExamples(
			plugin.getName(),
			Arrays.asList(scripts),
			Skript.getInstance().getScriptsFolder()
		);
	}

	/**
	 * Registers example scripts with Skript for this addon using a provider.
	 *
	 * @param provider Provider that supplies example scripts to install
	 * @throws IOException If an I/O error occurs while installing the scripts
	 */
	public void registerExampleScripts(ExampleScriptProvider provider) throws IOException {
		ExampleScriptManager manager = Skript.getExampleManager();
		if (manager == null)
			throw new IllegalStateException("Example script manager is not initialized");

		manager.installExamples(
			plugin.getName(),
			provider,
			Skript.getInstance().getScriptsFolder()
		);
	}


	@Nullable
	private File file;

	/**
	 * The first invocation of this method uses reflection to invoke the protected method {@link JavaPlugin#getFile()} to get the plugin's jar file.
	 * The file is then cached and returned upon subsequent calls to this method to reduce usage of reflection.
	 * Only nullable if there was an exception thrown.
	 * 
	 * @return The jar file of the plugin.
	 */
	@Nullable
	public File getFile() {
		if (file == null)
			file = Utils.getFile(plugin);
		return file;
	}

	//
	// Modern SkriptAddon Compatibility
	//

	@ApiStatus.Experimental
	static SkriptAddon fromModern(org.skriptlang.skript.addon.SkriptAddon addon) {
		return new SkriptAddon(JavaPlugin.getProvidingPlugin(addon.source()), addon);
	}

	@Override
	@ApiStatus.Experimental
	public Class<?> source() {
		return addon.source();
	}

	@Override
	@ApiStatus.Experimental
	public String name() {
		return addon.name();
	}

	@Override
	@ApiStatus.Experimental
	public <R extends Registry<?>> void storeRegistry(Class<R> registryClass, R registry) {
		addon.storeRegistry(registryClass, registry);
	}

	@Override
	@ApiStatus.Experimental
	public void removeRegistry(Class<? extends Registry<?>> registryClass) {
		addon.removeRegistry(registryClass);
	}

	@Override
	@ApiStatus.Experimental
	public boolean hasRegistry(Class<? extends Registry<?>> registryClass) {
		return addon.hasRegistry(registryClass);
	}

	@Override
	@ApiStatus.Experimental
	public <R extends Registry<?>> R registry(Class<R> registryClass) {
		return addon.registry(registryClass);
	}

	@Override
	@ApiStatus.Experimental
	public <R extends Registry<?>> R registry(Class<R> registryClass, Supplier<R> putIfAbsent) {
		return addon.registry(registryClass, putIfAbsent);
	}

	@Override
	@ApiStatus.Experimental
	public SyntaxRegistry syntaxRegistry() {
		return addon.syntaxRegistry();
	}

	@Override
	@ApiStatus.Experimental
	public Localizer localizer() {
		return addon.localizer();
	}

}
