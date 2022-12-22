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

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Stack;

/**
 * A stack that keeps track of what Skript is currently parsing.
 */
public class ParsingStack implements Iterable<ParsingStack.Element> {

	private final Stack<Element> stack;

	/**
	 * Creates an empty parsing stack.
	 */
	public ParsingStack() {
		this.stack = new Stack<>();
	}

	/**
	 * Creates a parsing stack containing all elements
	 * of another given parsing stack.
	 */
	public ParsingStack(ParsingStack parsingStack) {
		Stack<Element> stack = new Stack<>();
		stack.addAll(parsingStack.stack);
		this.stack = stack;
	}

	/**
	 * Removes and returns the top element of this stack.
	 */
	public Element pop() {
		return stack.pop();
	}

	/**
	 * Adds the given element to the top of the stack.
	 */
	public void push(Element e) {
		stack.push(e);
	}

	/**
	 * Check if this stack is empty.
	 */
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	/**
	 * Prints this stack to the given {@link PrintStream}.
	 */
	public void print(PrintStream printStream) {
		// Synchronized to assure it'll all be printed at once,
		//  PrintStream uses synchronization on itself internally, justifying warning suppression

		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (printStream) {
			printStream.println("Stack:");

			for (Element element : stack) {
				printStream.println("\t" + element.getSyntaxElementClass().getName() + " @ " + element.getPatternIndex());
			}
		}
	}

	@Override
	public Iterator<Element> iterator() {
		return stack.iterator();
	}

	/**
	 * A stack element, containing details about the syntax element it is about.
	 */
	public static class Element {

		private final SyntaxElementInfo<?> syntaxElementInfo;
		private final int patternIndex;

		public Element(SyntaxElementInfo<?> syntaxElementInfo, int patternIndex) {
			this.syntaxElementInfo = syntaxElementInfo;
			this.patternIndex = patternIndex;
		}

		/**
		 * Gets the raw {@link SyntaxElementInfo} of this stack element.
		 * <p>
		 * For ease of use, consider using other getters of this class.
		 *
		 * @see #getSyntaxElementClass()
		 * @see #getPattern()
		 */
		public SyntaxElementInfo<?> getSyntaxElementInfo() {
			return syntaxElementInfo;
		}

		/**
		 * Gets the index to the registered patterns for the syntax element
		 * of this stack element.
		 */
		public int getPatternIndex() {
			return patternIndex;
		}

		/**
		 * Gets the syntax element class of this stack element.
		 */
		public Class<? extends SyntaxElement> getSyntaxElementClass() {
			return syntaxElementInfo.getElementClass();
		}

		/**
		 * Gets the pattern that was matched for this stack element.
		 */
		public String getPattern() {
			return syntaxElementInfo.getPatterns()[patternIndex];
		}

		/**
		 * Gets all patterns registered with syntax element
		 * of this stack element.
		 */
		public String[] getPatterns() {
			return syntaxElementInfo.getPatterns();
		}

	}

}
