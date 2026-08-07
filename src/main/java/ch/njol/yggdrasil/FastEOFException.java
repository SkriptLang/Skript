package ch.njol.yggdrasil;

import java.io.EOFException;

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
