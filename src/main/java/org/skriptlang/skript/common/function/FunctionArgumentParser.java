package org.skriptlang.skript.common.function;

import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;

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
	 * The list of arguments that have been found so far.
	 */
	private final List<Argument<String>> arguments = new ArrayList<>();

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

	/**
	 * The char index.
	 */
	private int index = 0;

	/**
	 * The current character.
	 */
	private char c;

	/**
	 * Whether the current argument being parsed starts with a name declaration.
	 */
	private boolean nameFound = false;

	/**
	 * A builder which keeps track of the name part of an argument.
	 * <p>
	 * This builder may contain a part of the expression at the start of parsing an argument,
	 * when it is unclear whether we are currently parsing a name or not. On realization that
	 * this argument does not have a name, its contents are cleared.
	 * </p>
	 */
	private final StringBuilder namePart = new StringBuilder();

	/**
	 * A builder which keeps track of the expression part of an argument.
	 * <p>
	 * This builder may contain a part of the name at the start of parsing an argument,
	 * when it is unclear whether we are currently parsing a name or not. On realization that
	 * this argument has a name, its contents are cleared.
	 * </p>
	 */
	private final StringBuilder exprPart = new StringBuilder();

	/**
	 * Whether we are currently in a string or not.
	 * <p>
	 * To avoid parsing a comma in a string as the start of a new argument, we keep track of whether we're
	 * in a string or not to ignore commas found in strings.
	 * A new argument can only start when {@code nesting == 0 && !inString}.
	 * </p>
	 */
	private boolean inString = false;

	/**
	 * The level of nesting we are currently in.
	 * <p>
	 * The nesting level is increased when entering special expressions which may contain commas,
	 * thereby avoiding incorrectly parsing a comma in variables or parentheses as the start of a new argument.
	 * A new argument can only start when {@code nesting == 0 && !inString}.
	 * </p>
	 */
	private int nesting = 0;

	/**
	 * Parses the input string into arguments.
	 * <p>
	 * For every argument, during the parsing of the first few characters, one of the following things occurs.
	 * <ul>
	 *     <li>A legal parameter name character is encountered. The character is added to {@link #namePart} and
	 *     {@link #exprPart}.</li>
	 *     <li>An illegal parameter name character is encountered. This means that the previous data added to {@link #namePart}
	 *     cannot be a name. {@link #namePart} is cleared and the rest of the argument is parsed as the expression.</li>
	 *     <li>A colon {@code :} is encountered. When all previous characters for this argument match the requirements
	 *     for a parameter name, the name is stored in {@link #namePart} and the rest of the argument is parsed as the expression.</li>
	 *     <li>A comma {@code ,} is encountered. This means that the end of the argument has been reached. If no name was found,
	 *     the entire argument is parsed as {@link #exprPart}. If a name was found, {@link #exprPart} gets stored alongside {@link #namePart}.</li>
	 * </ul>
	 * </p>
	 */
	private void parse() {
		// if we have no args to parse, give up instantly
		if (args.isEmpty()) {
			return;
		}

		while (index < args.length()) {
			c = args.charAt(index);

			// first try to compile the name
			if (!nameFound) {
				// if a name matches the legal characters, update name part
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

				if (isSpecialCharacter(ArgumentType.UNNAMED)) {
					continue;
				}

				// given that the character did not match the legal name chars, reset name
				namePart.setLength(0);
				nextExpr();
				continue;
			}

			if (isSpecialCharacter(ArgumentType.NAMED)) {
				continue;
			}

			nextExpr(); // add to expression
		}

		// make sure to save the last argument
		if (nameFound) {
			save(ArgumentType.NAMED);
		} else {
			save(ArgumentType.UNNAMED);
		}
	}

	/**
	 * Manages special character handling by updating the {@link #nesting} and {@link #inString} variables.
	 *
	 * @param type The type of argument that is currently being parsed.
	 * @return True when {@link #c} is a special character, false if not.
	 */
	private boolean isSpecialCharacter(ArgumentType type) {
		// for strings
		if (!inString && c == '"') {
			nesting++;
			inString = true;
			nextExpr();
			return true;
		}

		if (inString && c == '"'
			&& index < args.length() - 1 && args.charAt(index + 1) != '"') { // allow double string char in strings
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

	/**
	 * Moves the parser to the next part of the expression that is being parsed.
	 */
	private void nextExpr() {
		exprPart.append(c);
		index++;
	}

	/**
	 * Saves the string parts stored in {@link #exprPart} (and optionally {@link #namePart}) as a new argument in
	 * {@link #arguments}. Then, all data for the current argument is cleared.
	 *
	 * @param type The type of argument to save as.
	 */
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
