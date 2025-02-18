package ch.njol.skript.localization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;

/**
 * Basic class to get text from the language file(s).
 * 
 * @author Peter Güttinger
 */
public class Message {
	
	// This is most likely faster than registering a listener for each Message
	private static final Collection<Message> messages = new ArrayList<>(50);
	private static boolean firstChange = true;
	static {
		Language.addListener(() -> {
			for (final Message m : messages) {
				synchronized (m) {
					m.revalidate = true;
				}
				if (firstChange && Skript.testing() && !Language.keyExists(m.key)) {
					Language.missingEntryError(m.key);
				}
			}
			firstChange = false;
		});
	}

	public final String key;
	@Nullable
	private String value;
	boolean revalidate = true;

	public Message(final String key) {
		this.key = "" + key.toLowerCase(Locale.ENGLISH);
		messages.add(this);

		if (Skript.testing() && Language.isInitialized() && !Language.keyExists(this.key))
			Language.missingEntryError(this.key);
	}

	/**
	 * @return The value of this message in the current language
	 */
	@Override
	public String toString() {
		validate();
		return value == null ? key : "" + value;
	}
	
	/**
	 * Gets the text this Message refers to. This method automatically revalidates the value if necessary.
	 * 
	 * @return This message's value or null if it doesn't exist.
	 */
	@Nullable
	public final String getValue() {
		validate();
		return value;
	}

	/**
	 * Gets the text this Message refers to. If value is null returns a default value.
	 *
	 * @param defaultValue The string this method refers to if value is null
	 * @return This message's value or default value if null
	 */
	public final String getValueOrDefault(String defaultValue) {
		validate();
		return value == null ? defaultValue : value;
	}

	/**
	 * Checks whether this value is set in the current language or the english default.
	 * 
	 * @return Whether this message will display an actual value instead of its key when used
	 */
	public final boolean isSet() {
		validate();
		return value != null;
	}
	
	/**
	 * Checks whether this message's value has changed and calls {@link #onValueChange()} if neccessary.
	 */
	protected synchronized void validate() {
		if (revalidate) {
			revalidate = false;
			value = Language.get_(key);
			onValueChange();
		}
	}
	
	/**
	 * Called when this Message's value changes. This is not neccessarily called for every language change, but only when the value is actually accessed and the language has
	 * changed since the last call of this method.
	 */
	protected void onValueChange() {}
	
}
