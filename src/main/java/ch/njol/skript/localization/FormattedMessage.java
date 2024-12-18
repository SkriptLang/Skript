package ch.njol.skript.localization;

import java.util.IllegalFormatException;
import java.util.concurrent.atomic.AtomicReference;

import ch.njol.skript.Skript;

public final class FormattedMessage extends Message {
	
	private final Object[] args;
	
	/**
	 * @param key
	 * @param args An array of Objects to replace into the format message, e.g. {@link AtomicReference}s.
	 */
	public FormattedMessage(final String key, final Object... args) {
		super(key);
		assert args.length > 0;
		this.args = args;
	}
	
	@Override
	public String toString() {
		try {
			String val = getValue();
			return val == null ? key : "" + String.format(val, args);
		} catch (final IllegalFormatException e) {
			String m = "The formatted message '" + key + "' uses an illegal format: " + e.getLocalizedMessage();
			Skript.adminBroadcast("<red>" + m);
			System.err.println("[Skript] " + m);
			e.printStackTrace();
			return "[ERROR]";
		}
	}
	
}
