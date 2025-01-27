package ch.njol.skript.lang.variable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.parser.ParserInstance;
import org.skriptlang.skript.lang.script.Script;

import java.util.regex.Pattern;

public class VariableData extends ParserInstance.Data {

	public VariableData(ParserInstance parserInstance) {
		super(parserInstance);
	}

	public boolean registerNamedVariable(Script source, String name, ClassInfo<?> type, boolean global) {
		return true;
	}

	public boolean isDeclared(VariableType type, String name) {
		return false;
	}

	public void declare(VariableType type, String name) {

	}

	public ClassInfo<?> getClassInfo(VariableType type, String name) {
		return null;
	}

}
