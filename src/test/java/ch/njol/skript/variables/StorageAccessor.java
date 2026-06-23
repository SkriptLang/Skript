package ch.njol.skript.variables;

/**
 * Class used by Variable tests to access package exclusive methods.
 */
public class StorageAccessor {

	public static void clearVariableStorages() {
		Variables.STORAGES.clear();
	}

}
