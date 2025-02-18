package ch.njol.skript.util.chat;

/**
 * Parse mode for links in chat messages.
 */
public enum LinkParseMode {
	
	/**
	 * Parses nothing automatically as a link.
	 */
	DISABLED,
	
	/**
	 * Parses everything that starts with http(s):// as a link.
	 */
	STRICT,
	
	/**
	 * Parses everything with "." as a link.
	 */
	LENIENT
}
