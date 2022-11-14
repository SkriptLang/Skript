/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.localization.Noun;

public enum Operator {
	
	PLUS('+', "add"),
	MINUS('-', "subtract"),
	MULT('*', "multiply"),
	DIV('/', "divide"),
	EXP('^', "exponent");

	private final Noun name;
	private final char sign;
	
	Operator(char sign, String node) {
		this.sign = sign;
		this.name = new Noun("operators." + node);
	}

	public Noun getName() {
		return name;
	}

	public char getSign() {
		return sign;
	}

	@Override
	public String toString() {
		return name.getSingular();
	}
}
