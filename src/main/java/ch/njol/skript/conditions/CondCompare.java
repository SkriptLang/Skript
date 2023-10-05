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
package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;

import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.ComparatorInfo;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;

import org.skriptlang.skript.lang.comparator.Comparators;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

@Name("Comparison")
@Description({"A very general condition, it simply compares two values. Usually you can only compare for equality (e.g. block is/isn't of &lt;type&gt;), " +
		"but some values can also be compared using greater than/less than. In that case you can also test for whether an object is between two others.",
		"Note: This is the only element where not all patterns are shown. It has actually another two sets of similar patters, " +
				"but with <code>(was|were)</code> or <code>will be</code> instead of <code>(is|are)</code> respectively, " +
				"which check different <a href='expressions.html#ExprTimeState'>time states</a> of the first expression."})
@Examples({"the clicked block is a stone slab or a double stone slab",
		"time in the player's world is greater than 8:00",
		"the creature is not an enderman or an ender dragon"})
@Since("1.0")
public class CondCompare extends Condition {
	
	private final static Patterns<Relation> patterns = new Patterns<>(new Object[][]{
			{"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) ((greater|more|higher|bigger|larger) than|above)|\\>) %objects%", Relation.GREATER},
			{"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as)|\\>=) %objects%", Relation.GREATER_OR_EQUAL},
			{"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) ((less|smaller|lower) than|below)|\\<) %objects%", Relation.SMALLER},
			{"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) (less|smaller|lower|below) [than] or (equal to|the same as)|\\<=) %objects%", Relation.SMALLER_OR_EQUAL},
			{"(1¦neither|) %objects% (2¦)((is|are) (not|4¦neither)|isn't|aren't|!=) [equal to] %objects%", Relation.EQUAL},
			{"(1¦neither|) %objects% (is|are|=) [(equal to|the same as)] %objects%", Relation.EQUAL},
			{"(1¦neither|) %objects% (is|are) between %objects% and %objects%", Relation.EQUAL},
			{"(1¦neither|) %objects% (2¦)(is not|are not|isn't|aren't) between %objects% and %objects%", Relation.EQUAL},
			
			{"(1¦neither|) %objects@-1% (was|were)(|2¦(n't| not|4¦ neither)) ((greater|more|higher|bigger|larger) than|above) %objects%", Relation.GREATER},
			{"(1¦neither|) %objects@-1% (was|were)(|2¦(n't| not|4¦ neither)) (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as) %objects%", Relation.GREATER_OR_EQUAL},
			{"(1¦neither|) %objects@-1% (was|were)(|2¦(n't| not|4¦ neither)) ((less|smaller|lower) than|below) %objects%", Relation.SMALLER},
			{"(1¦neither|) %objects@-1% (was|were)(|2¦(n't| not|4¦ neither)) (less|smaller|lower|below) [than] or (equal to|the same as) %objects%", Relation.SMALLER_OR_EQUAL},
			{"(1¦neither|) %objects@-1% (2¦)((was|were) (not|4¦neither)|wasn't|weren't) [equal to] %objects%", Relation.EQUAL},
			{"(1¦neither|) %objects@-1% (was|were) [(equal to|the same as)] %objects%", Relation.EQUAL},
			{"(1¦neither|) %objects@-1% (was|were) between %objects% and %objects%", Relation.EQUAL},
			{"(1¦neither|) %objects@-1% (2¦)(was not|were not|wasn't|weren't) between %objects% and %objects%", Relation.EQUAL},
			
			{"(1¦neither|) %objects@1% (will be|2¦(will (not|4¦neither) be|won't be)) ((greater|more|higher|bigger|larger) than|above) %objects%", Relation.GREATER},
			{"(1¦neither|) %objects@1% (will be|2¦(will (not|4¦neither) be|won't be)) (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as) %objects%", Relation.GREATER_OR_EQUAL},
			{"(1¦neither|) %objects@1% (will be|2¦(will (not|4¦neither) be|won't be)) ((less|smaller|lower) than|below) %objects%", Relation.SMALLER},
			{"(1¦neither|) %objects@1% (will be|2¦(will (not|4¦neither) be|won't be)) (less|smaller|lower|below) [than] or (equal to|the same as) %objects%", Relation.SMALLER_OR_EQUAL},
			{"(1¦neither|) %objects@1% (2¦)((will (not|4¦neither) be|won't be)|(isn't|aren't|is not|are not) (turning|changing) [in]to) [equal to] %objects%", Relation.EQUAL},
			{"(1¦neither|) %objects@1% (will be [(equal to|the same as)]|(is|are) (turning|changing) [in]to) %objects%", Relation.EQUAL},
			{"(1¦neither|) %objects@1% will be between %objects% and %objects%", Relation.EQUAL},
			{"(1¦neither|) %objects@1% (2¦)(will not be|won't be) between %objects% and %objects%", Relation.EQUAL}
	});
	
	static {
		Skript.registerCondition(CondCompare.class, patterns.getPatterns());
	}
	
	private Expression<?> first;
	private Expression<?> second;

	@Nullable
	private Expression<?> third;

	private Relation relation;

	@Nullable
	@SuppressWarnings("rawtypes")
	private Comparator comparator;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		first = vars[0];
		second = vars[1];
		if (vars.length == 3)
			third = vars[2];
		relation = patterns.getInfo(matchedPattern);
		if ((parser.mark & 0x2) != 0) // "not" somewhere in the condition
			setNegated(true);
		if ((parser.mark & 0x1) != 0) // "neither" on the left side
			setNegated(!isNegated());
		if ((parser.mark & 0x4) != 0) { // "neither" on the right side
			if (second instanceof ExpressionList)
				((ExpressionList<?>) second).invertAnd();
			if (third instanceof ExpressionList)
				((ExpressionList<?>) third).invertAnd();
		}
		final boolean b = init(parser.expr);
		final Expression<?> third = this.third;
		if (!b) {
			if (third == null && first.getReturnType() == Object.class && second.getReturnType() == Object.class) {
				return false;
			} else {
				Skript.error("Can't compare " + f(first) + " with " + f(second) + (third == null ? "" : " and " + f(third)), ErrorQuality.NOT_AN_EXPRESSION);
				return false;
			}
		}
		@SuppressWarnings("rawtypes")
		final Comparator comp = this.comparator;
		if (comp != null) {
			if (third == null) {
				if (!relation.isImpliedBy(Relation.EQUAL, Relation.NOT_EQUAL) && !comp.supportsOrdering()) {
					Skript.error("Can't test " + f(first) + " for being '" + relation + "' " + f(second), ErrorQuality.NOT_AN_EXPRESSION);
					return false;
				}
			} else {
				if (!comp.supportsOrdering()) {
					Skript.error("Can't test " + f(first) + " for being 'between' " + f(second) + " and " + f(third), ErrorQuality.NOT_AN_EXPRESSION);
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static String f(final Expression<?> e) {
		if (e.getReturnType() == Object.class)
			return e.toString(null, false);
		return Classes.getSuperClassInfo(e.getReturnType()).getName().withIndefiniteArticle();
	}
	
	@SuppressWarnings("unchecked")
	private boolean init(String expr) {
		RetainingLogHandler log = SkriptLogger.startRetainingLog();
		Expression<?> third = this.third;
		try {
			if (first.getReturnType() == Object.class) {
				Expression<?> expression = null;
				if (first instanceof UnparsedLiteral)
					expression = attemptReconstruction((UnparsedLiteral) first, second);
				if (expression == null)
					expression = first.getConvertedExpression(Object.class);
				if (expression == null) {
					log.printErrors();
					return false;
				}
				first = expression;
			}
			if (second.getReturnType() == Object.class) {
				Expression<?> expression = null;
				if (second instanceof UnparsedLiteral)
					expression = attemptReconstruction((UnparsedLiteral) second, first);
				if (expression == null)
					expression = second.getConvertedExpression(Object.class);
				if (expression == null) {
					log.printErrors();
					return false;
				}
				second = expression;
			}
			if (third != null && third.getReturnType() == Object.class) {
				Expression<?> expression = null;
				if (third instanceof UnparsedLiteral)
					expression = attemptReconstruction((UnparsedLiteral) third, first);
				if (expression == null)
					expression = third.getConvertedExpression(Object.class);
				if (expression == null) {
					log.printErrors();
					return false;
				}
				this.third = third = expression;
			}
			log.printLog();
		} finally {
			log.stop();
		}
		Class<?> firstReturnType = first.getReturnType();
		Class<?> secondReturnType = third == null ? second.getReturnType() : Utils.getSuperType(second.getReturnType(), third.getReturnType());
		if (firstReturnType == Object.class || secondReturnType == Object.class)
			return true;

		comparator = Comparators.getComparator(firstReturnType, secondReturnType);

		if (comparator == null) { // Try to re-parse with more context
			/*
			 * SkriptParser sees that CondCompare takes two objects. Most of the time,
			 * this works fine. However, when there are multiple conflicting literals,
			 * it just picks one of them at random.
			 * 
			 * If our other parameter is not a literal, we can try parsing the other
			 * explicitly with same return type. This is not guaranteed to succeed,
			 * but will in work in some cases that were previously ambiguous.
			 * 
			 * Some damage types not working (issue #2184) would be a good example
			 * of issues that SkriptParser's lack of context can cause.
			 */
			SimpleLiteral<?> reparsedSecond = reparseLiteral(firstReturnType, second);
			if (reparsedSecond != null) {
				second = reparsedSecond;
				comparator = Comparators.getComparator(firstReturnType, second.getReturnType());
			} else {
				SimpleLiteral<?> reparsedFirst = reparseLiteral(second.getReturnType(), first);
				if (reparsedFirst != null) {
					first = reparsedFirst;
					comparator = Comparators.getComparator(first.getReturnType(), secondReturnType);
				}
			}
			
		}

		return comparator != null;
	}

	/**
	 * Attempts to parse given expression again as a literal of given type.
	 * This will only work if the expression is a literal and its unparsed
	 * form can be accessed.
	 * @param <T> Wanted type.
	 * @param type Wanted class of literal.
	 * @param expr Expression we currently have.
	 * @return A literal value, or null if parsing failed.
	 */
	@Nullable
	private <T> SimpleLiteral<T> reparseLiteral(Class<T> type, Expression<?> expression) {
		Expression<?> source = expression;
		if (expression instanceof SimpleLiteral) // Only works for simple literals
			source = expression.getSource();

		// Try to get access to unparsed content of it
		if (source instanceof UnparsedLiteral) {
			String unparsed = ((UnparsedLiteral) source).getData();
			T data = Classes.parse(unparsed, type, ParseContext.DEFAULT);
			if (data != null) { // Success, let's make a literal of it
				return new SimpleLiteral<>(data, false, new UnparsedLiteral(unparsed));
			}
		}
		return null; // Context-sensitive parsing failed; can't really help it
	}

	/**
	 * Attempts to transform an UnparsedLiteral into a type that is comparable to another.
	 * For example 'fire' will be VisualEffect without this, but if the user attempts to compare 'fire'
	 * with a block. This method will see if 'fire' can be compared to the block, and it will find ItemType.
	 * Essentially solving something a human sees as comparable, but Skript doesn't understand.
	 * 
	 * @param one The UnparsedLiteral expression to attempt to reconstruct.
	 * @param two any expression to grab the return type from.
	 * @return The newly formed Literal, will be SimpleLiteral in most cases.
	 */
	@SuppressWarnings("unchecked")
	private Literal<?> attemptReconstruction(UnparsedLiteral one, Expression<?> two) {
		Expression<?> expression = null;
		// Must handle numbers first.
		expression = one.getConvertedExpression(Number.class);
		if (expression == null) {
			for (ClassInfo<?> classinfo : Classes.getClassInfos()) {
				if (classinfo.getParser() == null)
					continue;
				ComparatorInfo<?, ?> comparator = Comparators.getComparatorInfo(two.getReturnType(), classinfo.getC());
				if (comparator == null)
					continue;
				// We don't care about comparators that take in an object. This just causes more issues accepting and increases iterations by half.
				// Let getConvertedExpression deal with it in the end if no other possible reparses against the remaining classinfos exist.
				if (comparator.getFirstType() == Object.class)
					continue;
				expression = reparseLiteral(classinfo.getC(), one);
				if (expression != null)
					break;
			}
		}
		if (expression == null)
			expression = one.getConvertedExpression(two.getReturnType());
		return (Literal<?>) expression;
	}

	/*
	 * # := condition (e.g. is, is less than, contains, is enchanted with, has permission, etc.)
	 * !# := not #
	 * 
	 * a and b # x === a # x && b # x
	 * a or b # x === a # x || b # x
	 * a # x and y === a # x && a # y
	 * a # x or y === a # x || a # y
	 * a and b # x and y === a # x and y && b # x and y === a # x && a # y && b # x && b # y
	 * a and b # x or y === a # x or y && b # x or y
	 * a or b # x and y === a # x and y || b # x and y
	 * a or b # x or y === a # x or y || b # x or y
	 * 
	 * 
	 * a and b !# x === a !# x && b !# x
	 * neither a nor b # x === a !# x && b !# x		// nor = and
	 * a or b !# x === a !# x || b !# x
	 * 
	 * a !# x and y === a !# x || a !# y							// e.g. "player doesn't have 2 emeralds and 5 gold ingots" == "NOT(player has 2 emeralds and 5 gold ingots)" == "player doesn't have 2 emeralds OR player doesn't have 5 gold ingots"
	 * a # neither x nor y === a !# x && a !# y		// nor = or 	// e.g. "player has neither 2 emeralds nor 5 gold ingots" == "player doesn't have 2 emeralds AND player doesn't have 5 gold ingots"
	 * a # neither x nor y === a !# x && a !# y		// nor = or 	// e.g. "player is neither the attacker nor the victim" == "player is not the attacker AND player is not the victim"
	 * a !# x or y === a !# x && a !# y								// e.g. "player doesn't have 2 emeralds or 5 gold ingots" == "NOT(player has 2 emeralds or 5 gold ingots)" == "player doesn't have 2 emeralds AND player doesn't have 5 gold ingots"
	 * 
	 * a and b !# x and y === a !# x and y && b !# x and y === (a !# x || a !# y) && (b !# x || b !# y)
	 * a and b !# x or y === a !# x or y && b !# x or y
	 * a and b # neither x nor y === a # neither x nor y && b # neither x nor y
	 * 
	 * a or b !# x and y === a !# x and y || b !# x and y
	 * a or b !# x or y === a !# x or y || b !# x or y
	 * a or b # neither x nor y === a # neither x nor y || b # neither x nor y
	 * 
	 * neither a nor b # x and y === a !# x and y && b !# x and y		// nor = and
	 * neither a nor b # x or y === a !# x or y && b !# x or y			// nor = and
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean check(final Event e) {
		final Expression<?> third = this.third;
		return first.check(e, (Checker<Object>) o1 ->
			second.check(e, (Checker<Object>) o2 -> {
				if (third == null)
					return relation.isImpliedBy(comparator != null ? comparator.compare(o1, o2) : Comparators.compare(o1, o2));
				return third.check(e, (Checker<Object>) o3 -> {
					boolean isBetween;
					if (comparator != null) {
						isBetween =
							(Relation.GREATER_OR_EQUAL.isImpliedBy(comparator.compare(o1, o2)) && Relation.SMALLER_OR_EQUAL.isImpliedBy(comparator.compare(o1, o3)))
							// Check OPPOSITE (switching o2 / o3)
							|| (Relation.GREATER_OR_EQUAL.isImpliedBy(comparator.compare(o1, o3)) && Relation.SMALLER_OR_EQUAL.isImpliedBy(comparator.compare(o1, o2)));
					} else {
						isBetween =
							(Relation.GREATER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o2)) && Relation.SMALLER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o3)))
							// Check OPPOSITE (switching o2 / o3)
							|| (Relation.GREATER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o3)) && Relation.SMALLER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o2)));
					}
					return relation == Relation.NOT_EQUAL ^ isBetween;
				});
			}
		), isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		String s;
		final Expression<?> third = this.third;
		if (third == null)
			s = first.toString(e, debug) + " is " + (isNegated() ? "not " : "") + relation + " " + second.toString(e, debug);
		else
			s = first.toString(e, debug) + " is " + (isNegated() ? "not " : "") + "between " + second.toString(e, debug) + " and " + third.toString(e, debug);
		if (debug)
			s += " (comparator: " + comparator + ")";
		return s;
	}
	
}
