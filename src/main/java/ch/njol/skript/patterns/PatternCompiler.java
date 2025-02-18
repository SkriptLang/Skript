package ch.njol.skript.patterns;

import ch.njol.skript.lang.SkriptParser;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The pattern structure is a linked list of {@link PatternElement}s,
 * where {@link PatternElement#next} points to the next element to be matched,
 * which can be on an outer level, and where {@link PatternElement#originalNext} points
 * to the next element on the same level.
 */
public class PatternCompiler {

	/**
	 * @return an empty {@link PatternElement}
	 */
	private static PatternElement getEmpty() {
		return new LiteralPatternElement("");
	}

	/**
	 * Parses a pattern String into a {@link SkriptPattern}.
	 *
	 * @throws MalformedPatternException when the given pattern is malformed.
	 */
	public static SkriptPattern compile(String pattern) throws MalformedPatternException {
		AtomicInteger atomicInteger = new AtomicInteger(0);
		try {
			PatternElement first = compile(pattern, atomicInteger);
			return new SkriptPattern(first, atomicInteger.get());
		} catch (MalformedPatternException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new MalformedPatternException(pattern, "caught exception while compiling pattern", e);
		}
	}

	/**
	 * Compiles the given pattern String into a pattern.<br>
	 * The {@code expressionOffset} is to keep track of which index the next
	 * {@link TypePatternElement} should be initiated with.
	 * @return The first link of the {@link PatternElement} chain
	 */
	private static PatternElement compile(String pattern, AtomicInteger expressionOffset) {
		StringBuilder literalBuilder = new StringBuilder();
		PatternElement first = null;

		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c == '[') {
				if (literalBuilder.length() != 0) {
					first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				int end = SkriptParser.nextBracket(pattern, ']', c, i + 1, true);
				PatternElement patternElement = compile(pattern.substring(i + 1, end), expressionOffset);

				first = appendElement(first, new OptionalPatternElement(patternElement));

				i = end;
			} else if (c == '(') {
				if (literalBuilder.length() != 0) {
					first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				int end = SkriptParser.nextBracket(pattern, ')', c, i + 1, true);
				PatternElement patternElement = compile(pattern.substring(i + 1, end), expressionOffset);

				first = appendElement(first, new GroupPatternElement(patternElement));

				i = end;
			} else if (c == '|') {
				if (literalBuilder.length() != 0) {
					first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				PatternElement prevFirst = first;

				ChoicePatternElement choicePatternElement;
				if (first instanceof ChoicePatternElement) {
					choicePatternElement = (ChoicePatternElement) first;
				} else {
					first = choicePatternElement = new ChoicePatternElement();
					choicePatternElement.add(prevFirst != null ? prevFirst : getEmpty());
				}
				choicePatternElement.add(getEmpty());
			} else if (c == '¦' || c == ':') {
				String tag = literalBuilder.toString();
				literalBuilder = new StringBuilder();

				ParseTagPatternElement parseTagPatternElement;
				if (c == '¦') { // Old parse marks cannot be tags
					int mark;
					try {
						mark = Integer.parseInt(tag);
					} catch (NumberFormatException e) {
						throw new MalformedPatternException(pattern, "invalid parse mark at " + i, e);
					}
					parseTagPatternElement = new ParseTagPatternElement(mark);
				} else {
					parseTagPatternElement = new ParseTagPatternElement(tag);
				}

				first = appendElement(first, parseTagPatternElement);
			} else if (c == '%') {
				if (literalBuilder.length() != 0) {
					first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				int end = pattern.indexOf('%', i + 1);
				if (end == -1)
					throw new MalformedPatternException(pattern, "single percentage sign at " + i);
				int exprOffset = expressionOffset.getAndIncrement();
				TypePatternElement typePatternElement = TypePatternElement.fromString(pattern.substring(i + 1, end), exprOffset);

				first = appendElement(first, typePatternElement);

				i = end;
			} else if (c == '<') {
				if (literalBuilder.length() != 0) {
					first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
					literalBuilder = new StringBuilder();
				}

				int end = pattern.indexOf('>', i + 1);
				if (end == -1)
					throw new MalformedPatternException(pattern, "missing closing regex bracket '>' at " + i);

				Pattern regexPattern;
				try {
					regexPattern = Pattern.compile(pattern.substring(i + 1, end));
				} catch (final PatternSyntaxException e) {
					throw new MalformedPatternException(pattern, "invalid regex <" + pattern.substring(i + 1, end) + "> at " + i, e);
				}

				first = appendElement(first, new RegexPatternElement(regexPattern));

				i = end;
			} else if (c == '\\') {
				i++;
				literalBuilder.append(pattern.charAt(i));
			} else {
				literalBuilder.append(c);
			}
		}

		if (literalBuilder.length() != 0) {
			first = appendElement(first, new LiteralPatternElement(literalBuilder.toString()));
		}

		if (first == null) {
			return getEmpty();
		}

		return first;
	}

	/**
	 * Adds a {@link PatternElement} to the end of the list given by the first parameter.
	 * Returns the new first element of the list.
	 */
	private static PatternElement appendElement(@Nullable PatternElement first, PatternElement next) {
		if (first == null || (first instanceof LiteralPatternElement && first.next == null && ((LiteralPatternElement) first).isEmpty())) {
			return next;
		} else {
			if (first instanceof ChoicePatternElement) {
				ChoicePatternElement choicePatternElement = (ChoicePatternElement) first;
				PatternElement last = choicePatternElement.getLast();
				choicePatternElement.setLast(appendElement(last, next));
				return first;
			}

			PatternElement last = first;
			while (last.next != null)
				last = last.next;

			last.setNext(next);
			last.originalNext = next;
			return first;
		}
	}

}
