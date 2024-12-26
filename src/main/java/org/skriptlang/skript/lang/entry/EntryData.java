package org.skriptlang.skript.lang.entry;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import org.jetbrains.annotations.Nullable;

/**
 * EntryData is used for defining the different entries of for a {@link SectionNode}.
 * {@link org.skriptlang.skript.lang.structure.Structure}'s are a primary user of this system.
 * Take a look at this example:
 * <pre>
 * command /example: # this is the SectionNode
 *   description: this is an example of an entry
 *   trigger: # this is also an example of an entry
 *     # code goes here (not entry data!)
 * </pre>
 * From the above, it can be seen that EntryData is found at the level immediately after a {@link SectionNode}.
 * It can also be seen that entries come in many forms.
 * In fact, all entries are based upon a {@link Node}.
 * This could be something like a {@link ch.njol.skript.config.SimpleNode} or {@link SectionNode},
 *  but it may also be something totally different.
 * Every entry data class must define a validator-type method for {@link Node}s, along with
 *  a method of obtaining a value from that {@link Node}.
 * Every entry data instance must contain some sort of key. This key is the main identifier
 *  of an entry data instance within a {@link EntryValidator}.
 * @param <T> The type of the value returned by this entry data.
 */
public abstract class EntryData<T> {

	private final String key;
	@Nullable
	private final T defaultValue;
	private final boolean optional;

	public EntryData(String key, @Nullable T defaultValue, boolean optional) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.optional = optional;
	}

	/**
	 * @return The key that identifies and defines this entry data.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return The default value of this entry node to be used if {@link #getValue(Node)} is null,
	 *  or if the user does not include an entry for this entry data within their {@link SectionNode}.
	 */
	@Nullable
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return Whether this entry data <b>must</b> be included within a {@link SectionNode}.
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * Obtains a value from the provided node using the methods of this entry data.
	 * @param node The node to obtain a value from.
	 * @return The value obtained from the provided node.
	 */
	@Nullable
	public abstract T getValue(Node node);

	/**
	 * A method to be implemented by all entry data classes that determines whether
	 *  the provided node may be used with the entry data type to obtain a value.
	 * @param node The node to check.
	 * @return Whether the provided node may be used with this entry data to obtain a value.
	 */
	public abstract boolean canCreateWith(Node node);

}
