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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Arithmetic")
@Description("Arithmetic expressions, e.g. 1 + 2, (health of player - 2) / 3, etc.")
@Examples({"set the player's health to 10 - the player's health",
		"loop (argument + 2) / 5 times:",
		"\tmessage \"Two useless numbers: %loop-num * 2 - 5%, %2^loop-num - 1%\"",
		"message \"You have %health of player * 2% half hearts of HP!\""})
@Since("1.4.2")
@SuppressWarnings("null")
public class ExprArithmetic extends SimpleExpression<Number> {
	
	private static final Class<?>[] INTEGER_CLASSES = {Long.class, Integer.class, Short.class, Byte.class};
	
	private static class PatternInfo {
		public final Operator operator;
		public final boolean leftGrouped;
		public final boolean rightGrouped;
		
		public PatternInfo(Operator operator, boolean leftGrouped, boolean rightGrouped) {
			this.operator = operator;
			this.leftGrouped = leftGrouped;
			this.rightGrouped = rightGrouped;
		}
	}
	
	private final static Patterns<PatternInfo> patterns = new Patterns<>(new Object[][] {

		{"\\(%number%\\)[ ]+[ ]\\(%number%\\)", new PatternInfo(Operator.PLUS, true, true)},
		{"\\(%number%\\)[ ]+[ ]%number%", new PatternInfo(Operator.PLUS, true, false)},
		{"%number%[ ]+[ ]\\(%number%\\)", new PatternInfo(Operator.PLUS, false, true)},
		{"%number%[ ]+[ ]%number%", new PatternInfo(Operator.PLUS, false, false)},
		
		{"\\(%number%\\)[ ]-[ ]\\(%number%\\)", new PatternInfo(Operator.MINUS, true, true)},
		{"\\(%number%\\)[ ]-[ ]%number%", new PatternInfo(Operator.MINUS, true, false)},
		{"%number%[ ]-[ ]\\(%number%\\)", new PatternInfo(Operator.MINUS, false, true)},
		{"%number%[ ]-[ ]%number%", new PatternInfo(Operator.MINUS, false, false)},
		
		{"\\(%number%\\)[ ]*[ ]\\(%number%\\)", new PatternInfo(Operator.MULT, true, true)},
		{"\\(%number%\\)[ ]*[ ]%number%", new PatternInfo(Operator.MULT, true, false)},
		{"%number%[ ]*[ ]\\(%number%\\)", new PatternInfo(Operator.MULT, false, true)},
		{"%number%[ ]*[ ]%number%", new PatternInfo(Operator.MULT, false, false)},
		
		{"\\(%number%\\)[ ]/[ ]\\(%number%\\)", new PatternInfo(Operator.DIV, true, true)},
		{"\\(%number%\\)[ ]/[ ]%number%", new PatternInfo(Operator.DIV, true, false)},
		{"%number%[ ]/[ ]\\(%number%\\)", new PatternInfo(Operator.DIV, false, true)},
		{"%number%[ ]/[ ]%number%", new PatternInfo(Operator.DIV, false, false)},
		
		{"\\(%number%\\)[ ]^[ ]\\(%number%\\)", new PatternInfo(Operator.EXP, true, true)},
		{"\\(%number%\\)[ ]^[ ]%number%", new PatternInfo(Operator.EXP, true, false)},
		{"%number%[ ]^[ ]\\(%number%\\)", new PatternInfo(Operator.EXP, false, true)},
		{"%number%[ ]^[ ]%number%", new PatternInfo(Operator.EXP, false, false)},
		
	});
	
	static {
		Skript.registerExpression(ExprArithmetic.class, Number.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, patterns.getPatterns());
	}
	
	@SuppressWarnings("null")
	private Expression<? extends Number> first;
	@SuppressWarnings("null")
	private Expression<? extends Number> second;
	@SuppressWarnings("null")
	private Operator op;
	
	@SuppressWarnings("null")
	private Class<? extends Number> returnType;
	
	// A chain of expressions and operators, alternating between the two. Always starts and ends with an expression.
	private final List<Object> chain = new ArrayList<>();
	
	// A parsed chain, like a tree
	private ArithmeticGettable arithmeticGettable;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		first = (Expression<? extends Number>) exprs[0];
		second = (Expression<? extends Number>) exprs[1];
		
		PatternInfo patternInfo = patterns.getInfo(matchedPattern);
		op = patternInfo.operator;
		
		if (op == Operator.DIV || op == Operator.EXP) {
			returnType = Double.class;
		} else {
			Class<?> firstReturnType = first.getReturnType();
			Class<?> secondReturnType = second.getReturnType();
			
			boolean firstIsInt = false;
			boolean secondIsInt = false;
			for (final Class<?> i : INTEGER_CLASSES) {
				firstIsInt |= i.isAssignableFrom(firstReturnType);
				secondIsInt |= i.isAssignableFrom(secondReturnType);
			}
			
			returnType = firstIsInt && secondIsInt ? Long.class : Double.class;
		}
		
		// Chaining
		if (first instanceof ExprArithmetic && !patternInfo.leftGrouped) {
			chain.addAll(((ExprArithmetic) first).chain);
		} else {
			chain.add(first);
		}
		chain.add(op);
		if (second instanceof ExprArithmetic && !patternInfo.rightGrouped) {
			chain.addAll(((ExprArithmetic) second).chain);
		} else {
			chain.add(second);
		}
		
		arithmeticGettable = ArithmeticChain.parse(chain);
		
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected Number[] get(final Event e) {
		Number[] one = (Number[]) Array.newInstance(returnType, 1);
		
		one[0] = arithmeticGettable.get(e, returnType == Long.class);
		
		return one;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return returnType;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return first.toString(e, debug) + " " + op + " " + second.toString(e, debug);
	}
	
	@SuppressWarnings("null")
	@Override
	public Expression<? extends Number> simplify() {
		if (first instanceof Literal && second instanceof Literal)
			return new SimpleLiteral<>(getArray(null), Number.class, false);
		return this;
	}
	
}
