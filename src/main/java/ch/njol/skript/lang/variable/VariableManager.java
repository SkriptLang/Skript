package ch.njol.skript.lang.variable;

public interface VariableManager {

    Object get(VariableType variableType, String name);

	void set(VariableType variableType, String name, Object object);

	void delete(VariableType variableType, String name); // todo equivalent to set null I think

}
