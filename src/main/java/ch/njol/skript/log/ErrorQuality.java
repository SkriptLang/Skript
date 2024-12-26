package ch.njol.skript.log;

/**
 * The quality of a parse error.
 * 
 * @author Peter Güttinger
 */
public enum ErrorQuality {
	
	NONE, NOT_AN_EXPRESSION, SEMANTIC_ERROR;
	
	public int quality() {
		return ordinal();
	}
	
	@SuppressWarnings("null")
	public static ErrorQuality get(int quality) {
		return values()[quality];
	}
	
}
