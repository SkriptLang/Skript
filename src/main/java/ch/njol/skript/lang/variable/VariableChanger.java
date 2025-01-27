package ch.njol.skript.lang.variable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.SyntaxElement;

/**
 * A syntax that can change a variable.
 * Specifically, one that could assign a variable for the first time (e.g. 'set').
 */
public interface VariableChanger extends SyntaxElement {

	static void undeclaredVariableWarning(String name) {
		Skript.error("The variable '" + name + "' has not been declared yet.");
	}

}
