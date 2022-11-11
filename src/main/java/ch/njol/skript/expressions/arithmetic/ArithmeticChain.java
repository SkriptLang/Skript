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
import org.skriptlang.skript.lang.arithmetic.Arithmetic;

public class ArithmeticChain<L, R> implements ArithmeticGettable<L> {
	
	@SuppressWarnings("unchecked")
	private static final Checker<Object>[] CHECKERS = new Checker[]{
		o -> o.equals(Operator.PLUS) || o.equals(Operator.MINUS),
		o -> o.equals(Operator.MULT) || o.equals(Operator.DIV),
		o -> o.equals(Operator.EXP)
	};
	
	private final ArithmeticGettable<L> left;
	private final Operator operator;
	private final ArithmeticGettable<R> right;
	private final Arithmetic<L> arithmetic;
	
	public ArithmeticChain(ArithmeticGettable<L> left, Operator operator, ArithmeticGettable<R> right, Arithmetic<L> arithmetic) {
		this.left = left;
		this.operator = operator;
		this.right = right;
		this.arithmetic = arithmetic;
	}
	
	@Override
	public L get(Event event) {
		return arithmetic.calculate(left.get(event), operator, right.get(event));
	}
	
	@SuppressWarnings("unchecked")
	public static <L> ArithmeticGettable<L> parse(List<Object> chain, Arithmetic<L> arithmetic) {
		for (Checker<Object> checker : CHECKERS) {
			int lastIndex = Utils.findLastIndex(chain, checker);
			
			if (lastIndex != -1) {
				List<Object> leftChain = chain.subList(0, lastIndex);
				ArithmeticGettable<L> left = parse(leftChain, arithmetic);
				
				Operator operator = (Operator) chain.get(lastIndex);
				
				List<Object> rightChain = chain.subList(lastIndex + 1, chain.size());
				ArithmeticGettable<?> right = parse(rightChain, arithmetic);
				
				return new ArithmeticChain<>(left, operator, right, arithmetic);
			}
		}
		
		if (chain.size() != 1)
			throw new IllegalStateException();

		return event -> ((Expression<? extends L>) chain.get(0)).getSingle(event);
	}
	
}
