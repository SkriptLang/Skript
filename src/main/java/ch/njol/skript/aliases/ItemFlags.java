package ch.njol.skript.aliases;


/**
 * Contains bit mask flags for some item properties.
 */
public class ItemFlags {
	
	/**
	 * Durability of item changed.
	 */
	public static final int CHANGED_DURABILITY = 1;
	
	/**
	 * Changed tags other than durability.
	 */
	public static final int CHANGED_TAGS = 1 << 1;
}
