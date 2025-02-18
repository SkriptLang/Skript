package ch.njol.skript.update;


/**
 * State of updater.
 */
public enum UpdaterState {
	
	/**
	 * The updater has not been started.
	 */
	NOT_STARTED,
	
	/**
	 * Update check is currently in progress.
	 */
	CHECKING,
	
	/**
	 * An update is currently being downloaded.
	 */
	DOWNLOADING,
	
	/**
	 * The updater has done something, but is currently not doing anything.
	 */
	INACTIVE,
	
	/**
	 * The updater has encountered an error.
	 */
	ERROR
}
