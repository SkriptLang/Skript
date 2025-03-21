package ch.njol.skript.aliases;

/**
 * How well two items match each other.
 */
public enum MatchQuality {
	
	/**
	 * Everything matches.
	 */
	EXACT,
	
	/**
	 * The matched item has all metadata and block states that matcher has set
	 * to same values that matcher has. It also has additional metadata or
	 * block states.
	 */
	SAME_ITEM,
	
	/**
	 * The matched and matcher item share a material.
	 */
	SAME_MATERIAL,
	
	/**
	 * The items share nothing in common.
	 */
	DIFFERENT;
	
	public boolean isBetter(MatchQuality another) {
		return ordinal() < another.ordinal();
	}
	
	public boolean isAtLeast(MatchQuality another) {
		return ordinal() <= another.ordinal();
	}
}
