package ch.njol.yggdrasil;

/**
 * Thrown if the object(s) that should be saved/loaded with Yggdrasil do
 * not comply with its requirements, or if Yggdrasil is used incorrectly.
 * <p>
 * A detail message will always be supplied, so fixing these errors should be trivial.
 */
public final class YggdrasilException extends RuntimeException {
	
	private static final long serialVersionUID = -6130660396780458226L;
	
	public YggdrasilException(String message) {
		super(message);
	}
	
	public YggdrasilException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public YggdrasilException(Throwable cause) {
		super(cause.getClass().getSimpleName() + (cause.getMessage() == null ? "" : ": " + cause.getMessage()), cause);
	}
	
}
