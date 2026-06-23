package ch.njol.skript.variables;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.errorprone.annotations.ThreadSafe;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.VisibleForTesting;
import org.skriptlang.skript.addon.SkriptAddon;

/**
 * A variable storage is holds the means and methods of storing variables.
 * This is usually some sort of database, and could be as simply as a text file.
 * <p>
 * Variable storage itself is responsible for variable management, read and write requests,
 * and loading and unloading the variables to memory.
 * <p>
 * For storing variables on heap, see {@link VariablesMap} which provides thread safe
 * implementation for on heap variable storage.
 */
@ThreadSafe
public abstract class VariableStorage implements Closeable {

	/**
	 * Source of the variable storage.
	 */
	private final SkriptAddon source;

	/**
	 * The name of the database
	 */
	private String databaseName;

	/**
	 * The type of the database, i.e. CSV.
	 */
	private final String databaseType;

	/**
	 * The file associated with this variable storage.
	 * Can be {@code null} if no file is required.
	 */
	protected @Nullable File file;

	/**
	 * The pattern of the variable name this storage accepts.
	 * {@code null} for '{@code .*}' or '{@code .*}'.
	 */
	private @Nullable Pattern variableNamePattern;

	protected VariableStorage(SkriptAddon source, String type) {
		assert type != null;
		this.source = source;
		this.databaseType = type;
	}

	/**
	 * Get the config name of a database
	 * <p>
	 * Note: Returns the user set name for the database, ex:
	 * <pre>{@code
	 * default: <- Config Name
	 *    type: CSV
	 * }</pre>
	 * @return name of database
	 */
	protected final String getUserConfigurationName() {
		return databaseName;
	}

	/**
	 * Get the config type of a database
	 * 
	 * @return type of database
	 */
	protected final String getDatabaseType() {
		return databaseType;
	}

	/**
	 * @return The SkriptAddon instance that registered this VariableStorage.
	 */
	public final SkriptAddon getRegisterSource() {
		return source;
	}

	/**
	 * Gets the string value at the given key of the given section node.
	 *
	 * @param sectionNode the section node.
	 * @param key the key.
	 * @return the value, or {@code null} if the value was invalid,
	 * or not found.
	 */
	protected final @Nullable String getValue(SectionNode sectionNode, String key) {
		return getValue(sectionNode, key, String.class);
	}

	/**
	 * Gets the value at the given key of the given section node,
	 * parsed with the given type.
	 *
	 * @param sectionNode the section node.
	 * @param key the key.
	 * @param type the type.
	 * @return the parsed value, or {@code null} if the value was invalid,
	 * or not found.
	 * @param <T> the type.
	 */
	protected final <T> @Nullable T getValue(SectionNode sectionNode, String key, Class<T> type) {
		return getValue(sectionNode, key, type, true);
	}

	/**
	 * Gets the value at the given key of the given section node,
	 * parsed with the given type. Prints no errors, but can return null.
	 *
	 * @param sectionNode the section node.
	 * @param key the key.
	 * @param type the type.
	 * @return the parsed value, or {@code null} if the value was invalid,
	 * or not found.
	 * @param <T> the type.
	 */
	protected final <T> @Nullable T getOptional(SectionNode sectionNode, String key, Class<T> type) {
		return getValue(sectionNode, key, type, false);
	}

	/**
	 * Gets the value at the given key of the given section node,
	 * parsed with the given type.
	 *
	 * @param sectionNode the section node.
	 * @param key the key.
	 * @param type the type.
	 * @param error if Skript should print errors and stop loading.
	 * @return the parsed value, or {@code null} if the value was invalid,
	 * or not found.
	 * @param <T> the type.
	 */
	private <T> @Nullable T getValue(SectionNode sectionNode, String key, Class<T> type, boolean error) {
		String rawValue = sectionNode.getValue(key);
		if (rawValue == null) {
			if (error)
				Skript.error("The config is missing the entry for '" + key + "' in the database '" + databaseName + "'");
			return null;
		}

		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			T parsedValue = Classes.parse(rawValue, type, ParseContext.CONFIG);
			if (parsedValue == null && error)
				// Parsing failed
				log.printError("The entry for '" + key + "' in the database '" + databaseName + "' must be " +
					Classes.getSuperClassInfo(type).getName().withIndefiniteArticle());
			else
				log.printLog();

			return parsedValue;
		}
	}

	private static final Set<File> registeredFiles = ConcurrentHashMap.newKeySet();

	/**
	 * Loads the configuration for this variable storage
	 * from the given section node. Loads internal required values first in loadConfig.
	 * {@link #load(SectionNode)} is for extending classes.
	 * <p>
	 * This operation may be blocking if the variable storage deserializes some stored variables
	 * as some deserializers must be synced on the main thread.
	 *
	 * @param sectionNode the section node.
	 * @return whether the loading succeeded.
	 */
	@Blocking
	@VisibleForTesting
	public final boolean loadConfig(SectionNode sectionNode) {
		databaseName = sectionNode.getKey();
		String pattern = getValue(sectionNode, "pattern");
		if (pattern == null)
			return false;

		try {
			// Set variable name pattern, see field javadoc for explanation of null value
			variableNamePattern = pattern.equals(".*") || pattern.equals(".+") ? null : Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			Skript.error("Invalid pattern '" + pattern + "': " + e.getLocalizedMessage());
			return false;
		}

		if (requiresFile()) {
			// Initialize file
			String fileName = getValue(sectionNode, "file");
			if (fileName == null)
				return false;

			file = getFile(fileName).getAbsoluteFile();

			if (file.exists() && !file.isFile()) {
				Skript.error("The database file '" + file.getName() + "' must be an actual file, not a directory.");
				return false;
			}

			// Create the file if it does not exist yet
			try {
				if (!file.exists() && !file.createNewFile()) {
					Skript.error("Cannot create the database file '" + file.getName() + "'");
					return false;
				}
			} catch (IOException e) {
				Skript.error("Cannot create the database file '" + file.getName() + "': " + e.getLocalizedMessage());
				return false;
			}

			// Check for read & write permissions to the file
			if (!file.canWrite()) {
				Skript.error("Cannot write to the database file '" + file.getName() + "'!");
				return false;
			}
			if (!file.canRead()) {
				Skript.error("Cannot read from the database file '" + file.getName() + "'!");
				return false;
			}

			if (registeredFiles.contains(file)) {
				Skript.error("Database `" + databaseName + "` failed to load. The file `" + fileName + "` is already registered to another database.");
				return false;
			}
			registeredFiles.add(file);
		}

		// Load the entries custom to the variable storage
		return loadAbstract(sectionNode);
	}

	/**
	 * Used for abstract extending classes intercepting the
	 * configuration before sending to the final implementation class.
	 * <p>
	 * Override to use this method in AnotherAbstractClass;
	 * VariablesStorage -> AnotherAbstractClass -> FinalImplementation
	 * <p>
	 * This operation may be blocking if the variable storage deserializes some stored variables
	 * as some deserializers must be synced on the main thread.
	 *
	 * @param sectionNode the section node.
	 * @return whether the loading succeeded.
	 */
	@Blocking
	protected boolean loadAbstract(SectionNode sectionNode) {
		return load(sectionNode);
	}

	/**
	 * Loads variables stored here.
	 * <p>
	 * This operation may be blocking if the variable storage deserializes some stored variables
	 * as some deserializers must be synced on the main thread.
	 *
	 * @param sectionNode the section node.
	 * @return Whether the database could be loaded successfully,
	 * i.e. whether the config is correct and all variables could be loaded.
	 */
	@Blocking
	protected abstract boolean load(SectionNode sectionNode);

	/**
	 * Checks if this storage requires a file for storing its data.
	 *
	 * @return if this storage needs a file.
	 */
	protected abstract boolean requiresFile();

	/**
	 * Gets the file needed for this variable storage from the given file name.
	 * <p>
	 * Will only be called if {@link #requiresFile()} is {@code true}.
	 *
	 * @param fileName the given file name.
	 * @return the {@link File} object.
	 */
	protected abstract File getFile(String fileName);

	/**
	 * Reads a variable with given name from this storage.
	 * <p>
	 * The format of returned value follows {@link VariablesMap#getVariable(String)}.
	 * <p>
	 * This method must be thread safe.
	 *
	 * @param name name of the variable
	 * @return value of the variable
	 */
	public abstract @Nullable Object getVariable(String name);

	/**
	 * Sets the given variable to the given value.
	 * <p>
	 * This method accepts list variables,
	 * but these may only be set to {@code null}.
	 * <p>
	 * This method must be thread safe.
	 *
	 * @param name the variable name.
	 * @param value the variable value, {@code null} to delete the variable.
	 */
	public abstract void setVariable(String name, @Nullable Object value);

	/**
	 * Returns the number of currently loaded variables.
	 * <p>
	 * This number may not be fully accurate.
	 *
	 * @return number of loaded variables
	 */
	public abstract long loadedVariables();

	// TODO backups

	/**
	 * Checks if this variable storage accepts the given variable name.
	 *
	 * @param var the variable name.
	 * @return if this storage accepts the variable name.
	 * @see #variableNamePattern
	 */
	public boolean accept(@Nullable String var) {
		if (var == null)
			return false;
		return variableNamePattern == null || variableNamePattern.matcher(var).matches();
	}

	/**
	 * Returns the name pattern accepted by this variable storage
	 *
	 * @return the name pattern, or null if accepting all
	 */
	public @Nullable Pattern getNamePattern() {
		return variableNamePattern;
	}

	/**
	 * Called when Skript gets disabled.
	 */
	@Override
	public abstract void close();

}
