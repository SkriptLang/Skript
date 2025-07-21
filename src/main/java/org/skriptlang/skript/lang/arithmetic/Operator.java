package org.skriptlang.skript.lang.arithmetic;

import ch.njol.skript.localization.Noun;

public enum Operator {

	ADDITION('+', "add"),
	SUBTRACTION('-', "subtract"),
	MULTIPLICATION('*', "multiply"),
	DIVISION('/', "divide"),
	EXPONENTIATION('^', "exponentiate");

	private final char sign;
	private final Noun m_name;

	Operator(char sign, String node) {
		this.sign = sign;
		this.m_name = new Noun("operators." + node);
	}

	@Override
	public String toString() {
		return sign + "";
	}

	public String getName() {
		return m_name.toString();
	}

}
