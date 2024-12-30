package ch.njol.skript.update;


/**
 * Status of currently installed release.
 */
public enum ReleaseStatus {

	/**
	 * Latest release in channel. This is a good thing.
	 */
	LATEST("latest"),
	
	/**
	 * Old, probably unsupported release.
	 */
	OUTDATED("outdated"),
	
	/**
	 * Updates have not been checked, so it is not known if any exist.
	 */
	UNKNOWN("unknown"),
	
	/**
	 * Updates have been checked, but this release was not found at all.
	 * It might be not yet published.
	 */
	CUSTOM("custom"),

	/**
	 * Running a developer/nightly build, updates will not be checked.
	 */
	DEVELOPMENT("development");

	private final String name;

	ReleaseStatus(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
