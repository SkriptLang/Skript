package org.skriptlang.skript.variables.storage;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.SerializedVariable;
import com.google.errorprone.annotations.ThreadSafe;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Saves global variables somewhere where they survive a restart, such as a file or a database.
 * <p>
 * A storage does not hold variables. Skript keeps global variables in memory and serves all reads from there,
 * so a storage is only read once, at startup, and after that only receives the changes it has to save.
 * Which storage a variable is saved to is decided by the storage's name pattern in the config.
 * <p>
 * The lifecycle of a storage, in order:
 * <ol>
 *     <li><b>Configure:</b> Skript reads the storage's config section and calls {@link #configure(SectionNode)}.
 *     If it returns false, the storage is not used.</li>
 *     <li><b>Load:</b> {@link #load(StorageListener)} reads every stored variable and passes it to Skript.
 *     If it throws, Skript does not save to this storage, so that data which could not be read is not overwritten.</li>
 *     <li><b>Save:</b> while the server runs, changes to the storage's variables are queued for it,
 *     and the storage saves them on its own schedule, off the main thread.</li>
 *     <li><b>Close:</b> on shutdown, {@link #close()} saves what is left and releases the storage's resources.</li>
 * </ol>
 * <p>
 * Threads: Skript calls {@link #configure(SectionNode)}, {@link #load(StorageListener)} and {@link #close()}
 * once each and never concurrently. Changes are queued from any thread, and a storage must never do I/O
 * on the thread that made the change. {@link #flush()} may be called from any thread.
 * <p>
 * Storages are registered with {@code Variables.registerStorage} under one or more type names,
 * which users then pick with the {@code type} option of a storage's config section.
 */
@ThreadSafe
public abstract class VariableStorage implements Closeable {

	private final SkriptAddon registeringAddon;

	private final String type;

	private String name;

	protected VariableStorage(SkriptAddon registeringAddon, String type) {
		this.registeringAddon = registeringAddon;
		this.type = type;
	}

	/**
	 * Reads the configuration of this storage from the given node.
	 * Do not do any reading of stored data here; that is done in {@link #load(StorageListener)}.
	 * Log issues with {@link Skript#error(String)} and return false if the configuration is invalid.
	 *
	 * @param node the configuration node
	 * @return true if the configuration was valid and the storage is ready to be used, false if it was invalid and the storage should be disabled
	 */
	protected abstract boolean configure(SectionNode node);

	/**
	 * Initially loads the variables from this storage and passes them to Skript. Called once after configuration.
	 * This is allowed to block for as long as necessary, though as always, faster is better.
	 * Any variable loaded should be passed to {@link StorageListener#loaded(String, SerializedVariable.Value)} as it is loaded.
	 *
	 * @param listener the listener to pass the variables to
	 * @throws IOException if the stored data could not be read completely
	 */
	protected abstract void load(StorageListener listener) throws IOException;

	/**
	 * @return Whether this storage requires a file for it to work.
	 */
	protected abstract boolean requiresFile();

	/**
	 * Resolves the configured file name to the file this storage uses.
	 * Only called if {@link #requiresFile()} returns true.
	 *
	 * @param fileName the name of the file, as configured in the storage's configuration
	 * @return the file that this storage uses to store its variables
	 */
	protected abstract File getFile(String fileName);

	/**
	 * Flushes any pending changes to this storage. Called when Skript is saving variables.
	 * Any pending changes should be saved before this method returns.
	 *
	 * @return true if the flush was successful, false if it could not confirm that the changes are stored,
	 * or this storage doesn't support flushing
	 */
	public boolean flush() {
		return false;
	}

	/**
	 * Closes this storage and releases any resources it holds. After this method is called, the storage should not be used again.
	 * Any pending changes should be saved before this method returns.
	 * Called when Skript is shutting down, or if {@link #load(StorageListener)} fails.
	 */
	@Override
	public abstract void close();

	/**
	 * @return the addon that registered this storage.
	 */
	public final SkriptAddon getRegisteringAddon() {
		return registeringAddon;
	}

	/**
	 * @return the type of this storage, e.g. "mysql"
	 */
	public final String getType() {
		return type;
	}

	/**
	 * May return null if the storage has not started configuration yet.
	 *
	 * @return the name of this storage, e.g. "default"
	 */
	public final @UnknownNullability String getName() {
		return name;
	}

}
