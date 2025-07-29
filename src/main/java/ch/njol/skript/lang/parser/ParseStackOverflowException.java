package ch.njol.skript.lang.parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * An exception noting that a {@link StackOverflowError} has occurred
 * during Skript parsing. Contains information about the {@link ParsingStack}
 * from when the stack overflow occurred.
 */
public class ParseStackOverflowException extends RuntimeException {

	protected final ParsingStack parsingStack;

	public ParseStackOverflowException(StackOverflowError cause, ParsingStack parsingStack) {
		super(createMessage(parsingStack), cause);
		this.parsingStack = parsingStack;
	}

	/**
	 * Creates the exception message from the given {@link ParsingStack}.
	 */
	private static String createMessage(ParsingStack stack) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		PrintStream printStream = new PrintStream(stream);
		stack.print(printStream);

		return stream.toString();
	}

}
