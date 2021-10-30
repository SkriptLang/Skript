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

import java.util.List;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;

public class ArithmeticChain implements ArithmeticGettable {
	
	@SuppressWarnings("unchecked")
	private static final Checker<Object>[] CHECKERS = new Checker[]{
		o -> o.equals(Operator.PLUS) || o.equals(Operator.MINUS),
		o -> o.equals(Operator.MULT) || o.equals(Operator.DIV),
		o -> o.equals(Operator.EXP)
	};
	
	private final ArithmeticGettable left;
	private final Operator operator;
	private final ArithmeticGettable right;
	
	public ArithmeticChain(ArithmeticGettable left, Operator operator, ArithmeticGettable right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}
	
	@Override
	public Number get(Event event, boolean integer) {
		return operator.calculate(left.get(event, integer), right.get(event, integer), integer);
	}
	
	@SuppressWarnings("unchecked")
	public static ArithmeticGettable parse(List<Object> chain) {
		for (Checker<Object> checker : CHECKERS) {
			int lastIndex = Utils.findLastIndex(chain, checker);
			
			if (lastIndex != -1) {
				List<Object> leftChain = chain.subList(0, lastIndex);
				ArithmeticGettable left = parse(leftChain);
				
				Operator operator = (Operator) chain.get(lastIndex);
				
				List<Object> rightChain = chain.subList(lastIndex + 1, chain.size());
				ArithmeticGettable right = parse(rightChain);
				
				return new ArithmeticChain(left, operator, right);
			}
		}
		
		if (chain.size() != 1)
			throw new IllegalStateException();
		
		return new NumberExpressionInfo((Expression<? extends Number>) chain.get(0));
	}
	
}
