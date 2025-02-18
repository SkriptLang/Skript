package ch.njol.skript.lang.parser;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.util.Kleenean;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A stack that keeps track of what Skript is currently parsing.
 * <p>
 * When accessing the stack from within
 * {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)},
 * the stack element corresponding to that {@link SyntaxElement} is <b>not</b>
 * on the parsing stack.
 */
public class ParsingStack implements Iterable<ParsingStack.Element> {

	private final LinkedList<Element> stack;

	/**
	 * Creates an empty parsing stack.
	 */
	public ParsingStack() {
		this.stack = new LinkedList<>();
	}

	/**
	 * Creates a parsing stack containing all elements
	 * of another given parsing stack.
	 */
	public ParsingStack(ParsingStack parsingStack) {
		this.stack = new LinkedList<>(parsingStack.stack);
	}

	/**
	 * Removes and returns the top element of this stack.
	 *
	 * @throws IllegalStateException if the stack is empty.
	 */
	public Element pop() throws IllegalStateException {
		if (stack.isEmpty()) {
			throw new IllegalStateException("Stack is empty");
		}

		return stack.pop();
	}

	/**
	 * Returns the element at the given index in the stack,
	 * starting with the top element at index 0.
	 *
	 * @param index the index in stack.
	 * @throws IndexOutOfBoundsException if the index is not appointed
	 *                                   to an element in the stack.
	 */
	public Element peek(int index) throws IndexOutOfBoundsException {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}

		return stack.get(index);
	}

	/**
	 * Returns the top element of the stack.
	 * Equivalent to {@code peek(0)}.
	 *
	 * @throws IllegalStateException if the stack is empty.
	 */
	public Element peek() throws IllegalStateException {
		if (stack.isEmpty()) {
			throw new IllegalStateException("Stack is empty");
		}

		return stack.peek();
	}

	/**
	 * Adds the given element to the top of the stack.
	 */
	public void push(Element element) {
		stack.push(element);
	}

	/**
	 * Check if this stack is empty.
	 */
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	/**
	 * Gets the size of the stack.
	 */
	public int size() {
		return stack.size();
	}

	/**
	 * Prints this stack to the given {@link PrintStream}.
	 *
	 * @param printStream a {@link PrintStream} to print the stack to.
	 */
	public void print(PrintStream printStream) {
		// Synchronized to assure it'll all be printed at once,
		//  PrintStream uses synchronization on itself internally, justifying warning suppression

		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (printStream) {
			printStream.println("Stack:");

			if (stack.isEmpty()) {
				printStream.println("<empty>");
			} else {
				for (Element element : stack) {
					printStream.println("\t" + element.getSyntaxElementClass().getName() +
						" @ " + element.patternIndex());
				}
			}
		}
	}

	/**
	 * Iterate over the stack, starting at the top.
	 */
	@Override
	public Iterator<Element> iterator() {
		return Collections.unmodifiableList(stack).iterator();
	}

	/**
	 * A stack element, containing details about the syntax element it is about.
	 */
	public record Element(SyntaxElementInfo<?> syntaxElementInfo, int patternIndex) {

		public Element {
			assert patternIndex >= 0 && patternIndex < syntaxElementInfo.getPatterns().length;
		}

		/**
		 * Gets the raw {@link SyntaxElementInfo} of this stack element.
		 * <p>
		 * For ease of use, consider using other getters of this class.
		 *
		 * @see #getSyntaxElementClass()
		 * @see #getPattern()
		 */
		@Override
		public SyntaxElementInfo<?> syntaxElementInfo() {
			return syntaxElementInfo;
		}

		/**
		 * Gets the index to the registered patterns for the syntax element
		 * of this stack element.
		 */
		@Override
		public int patternIndex() {
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
		 * Gets all patterns registered with the syntax element
		 * of this stack element.
		 */
		public String[] getPatterns() {
			return syntaxElementInfo.getPatterns();
		}

	}

}
