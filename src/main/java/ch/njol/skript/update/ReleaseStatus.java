package ch.njol.skript.update;


/**
 * Status of currently installed release.
 */
public enum ReleaseStatus {
	
	/**
	 * Latest release in channel. This is a good thing.
	 */
	LATEST,
	
	/**
	 * Old, probably unsupported release.
	 */
	OUTDATED,
	
	/**
	 * Updates have not been checked, so it not known if any exist.
	 */
	UNKNOWN,
	
	/**
	 * Updates have been checked, but this release was not found at all.
	 * It might be not yet published.
	 */
	CUSTOM,

	/**
	 * Running a developer/nightly build, updates will not be checked.
	 */
	DEVELOPMENT
}
