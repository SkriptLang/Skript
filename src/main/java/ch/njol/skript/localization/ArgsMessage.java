package ch.njol.skript.localization;

import java.util.IllegalFormatException;

import ch.njol.skript.Skript;

public final class ArgsMessage extends Message {
	
	public ArgsMessage(String key) {
		super(key);
	}
	
	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}
	
	public String toString(Object... args) {
		try {
			String val = getValue();
			return val == null ? key : "" + String.format(val, args);
		} catch (IllegalFormatException e) {
			String m = "The formatted message '" + key + "' uses an illegal format: " + e.getLocalizedMessage();
			Skript.adminBroadcast("<red>" + m);
			System.err.println("[Skript] " + m);
			e.printStackTrace();
			return "[ERROR]";
		}
	}
	
}
