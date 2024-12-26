package org.skriptlang.skript.lang.arithmetic;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.localization.Noun;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
