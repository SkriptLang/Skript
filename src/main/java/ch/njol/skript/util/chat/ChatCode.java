package ch.njol.skript.util.chat;

import org.jetbrains.annotations.Nullable;

/**
 * Chat codes; includes color codes (<a href="https://wiki.vg/Chat#Colors">reference</a>)
 * and also, some formatting codes (mostly <a href="https://wiki.vg/Chat">this</a>)
 */
public interface ChatCode {
	
	/**
	 * Applies style of this chat code to given component.
	 * @param component Component to update.
	 * @param param String parameter. May be empty string.
	 */
	void updateComponent(MessageComponent component, String param);
	
	/**
	 * Checks if this chat code takes a string parameter. If yes, scripters
	 * will use it like:
	 * <code>&lt;name:param&gt;</code>
	 * @return
	 */
	boolean hasParam();
	
	/**
	 * Gets color code of this chat code. Skript will apply it automatically.
	 * Note that setting color code is usually not useful for addon developers,
	 * because Skript supports all colors that Minecraft client does.
	 * 
	 * <p>If null is returned, {@link #updateComponent(MessageComponent, String)}
	 * is called instead. You probably want that, as it is more versatile.
	 * @return Color code.
	 */
	@Nullable
	String getColorCode();
	
	/**
	 * Name to be used in scripts. if {@link #isLocalized()} is true, this is
	 * used as a language file key instead.
	 * @return Name in language file.
	 */
	@Nullable
	String getLangName();
	
	/**
	 * For internal usage.
	 * @return True for Skript's color codes.
	 */
	default boolean isLocalized() {
		return false;
	}
	
	/**
	 * Gets the color char that is an alternative way to use this chat code.
	 * Return 0 unless you wish to use a color char.
	 * 
	 * <p>If not 0, it can be used in scripts in following way:
	 * <code>&amp;x</code> or <code>§x</code>, if the code is <code>x</code>.
	 * It is not case sensitive if you return a character from which there is
	 * upper case character available. If you return the upper case character,
	 * lower case variant cannot be used in scripts (so don't do that).
	 * @return Color char.
	 */
	char getColorChar();
	
}
