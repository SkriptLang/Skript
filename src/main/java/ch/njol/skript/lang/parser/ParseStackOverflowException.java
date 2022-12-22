/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang.parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * An exception noting that a {@link StackOverflowError} has occurred
 * during Skript parsing. Contains information about the {@link ParsingStack}
 * from when the stack overflow occurred.
 */
public class ParseStackOverflowException extends RuntimeException {

	private final ParsingStack parsingStack;

	public ParseStackOverflowException(StackOverflowError cause, ParsingStack parsingStack) {
		super(createMessage(parsingStack), cause);
		this.parsingStack = parsingStack;
	}

	/**
	 * Creates the exception message from the given {@link ParsingStack}.
	 */
	private static String createMessage(ParsingStack stack) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		PrintStream printStream = new PrintStream(baos);
		stack.print(printStream);

		return baos.toString();
	}

}
