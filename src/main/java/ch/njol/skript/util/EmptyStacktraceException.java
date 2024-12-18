package ch.njol.skript.util;

/**
 * @author Peter Güttinger
 */
public class EmptyStacktraceException extends RuntimeException {
	private static final long serialVersionUID = 5107844579323721139L;
	
	public EmptyStacktraceException() {
		super(null, null, true, false);
	}
}
