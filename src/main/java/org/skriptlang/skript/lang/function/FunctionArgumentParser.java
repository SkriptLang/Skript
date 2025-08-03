package org.skriptlang.skript.lang.function;

import org.skriptlang.skript.lang.function.FunctionReference.Argument;
import org.skriptlang.skript.lang.function.FunctionReference.ArgumentType;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the arguments of a function reference.
 */
final class FunctionArgumentParser {

	/**
	 * The input string.
	 */
	private final String args;

	/**
	 * The list of unmapped arguments.
	 */
	private final List<Argument<String>> arguments = new ArrayList<>();

	/**
	 * The char index.
	 */
	private int index = 0;

	/**
	 * Constructs a new function argument parser based on the
	 * input string and instantly calculates the result.
	 *
	 * @param args The input string.
	 */
	public FunctionArgumentParser(String args) {
		this.args = args;

		parse();
	}

	private char c;
	private boolean nameFound = false;
	private final StringBuilder namePart = new StringBuilder();
	private final StringBuilder exprPart = new StringBuilder();

	private boolean inString = false;
	private int nesting = 0;

	private void parse() {
		while (index < args.length()) {
			c = args.charAt(index);

			// first try to compile the name
			if (!nameFound) {
				c = args.charAt(index);

				if (c == '_' || Character.isLetterOrDigit(c)) {
					namePart.append(c);
					exprPart.append(c);
					index++;
					continue;
				}

				// then if we have a name, start parsing the second part
				if (nesting == 0 && c == ':' && !namePart.isEmpty()) {
					exprPart.setLength(0);
					index++;
					nameFound = true;
					continue;
				}

				if (handleSpecialCharacters(ArgumentType.UNNAMED)) continue;

				namePart.setLength(0);
				nextExpr();
				continue;
			}

			if (handleSpecialCharacters(ArgumentType.NAMED)) continue;

			nextExpr();
		}

		if (args.isEmpty()) {
			return;
		}

		if (nameFound) {
			save(ArgumentType.NAMED);
		} else {
			save(ArgumentType.UNNAMED);
		}
	}

	private boolean handleSpecialCharacters(ArgumentType type) {
		// for strings
		if (!inString && c == '"') {
			nesting++;
			inString = true;
			nextExpr();
			return true;
		}

		if (inString && c == '"'
				&& index < args.length() - 1 && args.charAt(index + 1) != '"') {
			nesting--;
			inString = false;
			nextExpr();
			return true;
		}

		if (c == '(' || c == '{') {
			nesting++;
			nextExpr();
			return true;
		}

		if (c == ')' || c == '}') {
			nesting--;
			nextExpr();
			return true;
		}

		if (nesting == 0 && c == ',') {
			save(type);
			return true;
		}

		return false;
	}

	private void save(ArgumentType type) {
		if (type == ArgumentType.UNNAMED) {
			arguments.add(new Argument<>(ArgumentType.UNNAMED, null, exprPart.toString().trim()));
		} else {
			arguments.add(new Argument<>(ArgumentType.NAMED, namePart.toString().trim(), exprPart.toString().trim()));
		}

		namePart.setLength(0);
		exprPart.setLength(0);
		index++;
		nameFound = false;
	}

	private void nextExpr() {
		exprPart.append(c);
		index++;
	}

	/**
	 * Returns all arguments.
	 *
	 * @return All arguments.
	 */
	public Argument<String>[] getArguments() {
		//noinspection unchecked
		return (Argument<String>[]) arguments.toArray(new Argument[0]);
	}

}