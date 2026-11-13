package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.ApiStatus;

/**
 * Receives the variables a storage reads in {@link VariableStorage#load(LoadListener)}.
 * <p>
 * Skript implements this interface and gives one to each storage when loading it. It is only valid until
 * {@code load} returns, so don't keep it, and call it only from the thread that called {@code load}.
 * A storage that reads in parallel has to pass what it read back to that thread.
 * <p>
 * Changes to a storage's data made from outside this server after loading are reported with
 * {@link VariableStorage#reportUpdated} and {@link VariableStorage#reportCleared} instead.
 */
@ApiStatus.NonExtendable
public interface LoadListener {

	/**
	 * Gives Skript a variable read from this storage.
	 * Call it once for each stored variable.
	 * <p>
	 * Report every stored variable, even if its name no longer starts with one of this storage's prefixes;
	 * Skript then moves it to the storage it belongs to.
	 * Skript deserializes the value, so a value whose type no longer exists is skipped but left in the storage.
	 *
	 * @param name  the name of the variable
	 * @param value the serialized value of the variable
	 */
	void loaded(String name, SerializedVariable.Value value);

}
