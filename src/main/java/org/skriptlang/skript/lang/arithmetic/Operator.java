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
package org.skriptlang.skript.lang.arithmetic;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.localization.Noun;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum Operator {

	ADDITION('+', "add"),
	SUBTRACTION('-', "subtract"),
	MULTIPLICATION('*', "multiply"),
	DIVISION('/', "divide"),
	EXPONENTIATION('^', "exponent");

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

	public boolean acceptsClass(Class<?> type, Class<?>... classes) {
		List<OperationInfo<?, ?, ?>> infoList = Arithmetics.getOperations(this, type);
		if (infoList.size() == 0)
			return false;

		for (Class<?> aClass : classes) {
			for (OperationInfo<?, ?, ?> info : infoList) {
				if (info.getRight().isAssignableFrom(aClass))
					return true;
			}
		}
		return false;
	}

}
