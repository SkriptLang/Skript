package ch.njol.skript.lang.variable;

public enum VariableTypes implements VariableType {
	REGULAR("&"),
	GLOBAL("&"),
	LOCAL("_");

	private final String prefix;

	VariableTypes(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String prefix() {
		return prefix;
	}
}
