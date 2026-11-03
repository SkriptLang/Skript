package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Lets a variable storage pass stored variables to Skript: the variables it reads at startup,
 * and changes made to its data from outside this server.
 * <p>
 * Skript implements this interface and gives it to each storage.
 * Each call only updates Skript's variables and is never passed back to any storage,
 * so the storage must already hold the change it reports.
 * Skript deserializes the values, on the main thread for types that require it.
 */
@ApiStatus.NonExtendable
public interface StorageListener {

	/**
	 * Gives Skript a variable read from this storage while loading.
	 * Call it once for each stored variable, only from within {@code VariableStorage#load(StorageListener)}.
	 * <p>
	 * Report every stored variable, even if its name no longer matches this storage's pattern;
	 * Skript then moves it to the storage it belongs to.
	 *
	 * @param name  the name of the variable
	 * @param value the serialized value of the variable
	 */
	void loaded(String name, SerializedVariable.Value value);

	/**
	 * Tells Skript that a variable in this storage was changed from outside this server,
	 * for example by another server writing to the same SQL database.
	 * Only call it after loading has finished. It may be called from any thread.
	 * <p>
	 * Skript updates the variable and drops any change to it that this server has not saved yet,
	 * so the stored value is kept.
	 *
	 * @param name  the name of the variable
	 * @param value the new serialized value of the variable, or {@code null} if the variable was deleted
	 */
	void updated(String name, SerializedVariable.@Nullable Value value);

	/**
	 * Tells Skript that a list in this storage was deleted from outside this server.
	 * Only call it after loading has finished. It may be called from any thread.
	 * <p>
	 * Skript deletes the list's variables, including those in nested lists,
	 * and drops any changes to them that this server has not saved yet.
	 * A variable with the list's name minus the {@code ::*}, such as {@code a} for {@code a::*}, is not affected.
	 *
	 * @param listName the full name of the list, including the {@code ::*} ending, e.g. {@code a::*}
	 */
	void cleared(String listName);

}
