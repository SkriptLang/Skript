package ch.njol.yggdrasil;

import java.io.EOFException;

/**
 * An EOFException with an empty {@link #fillInStackTrace()} implementation.
 * <p>
 * Yggdrasil uses {@link EOFException} as a breakpoint, however the {@link #fillInStackTrace()} method causes
 * slowdowns while Skript loads variables. This method is not needed as the exception is only being used as a
 * breakpoint, not an actual exception.
 */
public class FastEOFException extends EOFException {

	public FastEOFException() {
		super();
	}

	public FastEOFException(String message) {
		super(message);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

}
