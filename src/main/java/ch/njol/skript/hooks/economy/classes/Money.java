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
package ch.njol.skript.hooks.economy.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.data.JavaClasses;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.ClassInfo;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.converter.Converter;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.util.StringUtils;
import org.skriptlang.skript.lang.comparator.Relation;

/**
 * @author Peter Güttinger
 */
public class Money {
	static {
		Classes.registerClass(new ClassInfo<>(Money.class, "money")
				.user("money")
				.name("Money")
				.description("A certain amount of money. Please note that this differs from <a href='#number'>numbers</a> as it includes a currency symbol or name, but usually the two are interchangeable, e.g. you can both <code>add 100$ to the player's balance</code> and <code>add 100 to the player's balance</code>.")
				.usage("&lt;number&gt; $ or $ &lt;number&gt;, where '$' is your server's currency, e.g. '10 rupees' or '£5.00'")
				.examples("add 10£ to the player's account",
						"remove Fr. 9.95 from the player's money",
						"set the victim's money to 0",
						"increase the attacker's balance by the level of the victim * 100")
				.since("2.0")
				.before("itemtype", "itemstack")
				.requiredPlugins("Vault", "an economy plugin that supports Vault")
				.parser(new Parser<Money>() {
					@Override
					@Nullable
					public Money parse(final String s, final ParseContext context) {
						return Money.parse(s);
					}
					
					@Override
					public String toString(final Money m, final int flags) {
						return m.toString();
					}
					
					@Override
					public String toVariableNameString(final Money o) {
						return "money:" + o.amount;
					}
                }));
		
		Comparators.registerComparator(Money.class, Money.class, new Comparator<Money, Money>() {
			@Override
			public Relation compare(final Money m1, final Money m2) {
				return Relation.get(m1.amount - m2.amount);
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		Comparators.registerComparator(Money.class, Number.class, new Comparator<Money, Number>() {
			@Override
			public Relation compare(final Money m, final Number n) {
				return Relation.get(m.amount - n.doubleValue());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		Converters.registerConverter(Money.class, Double.class, new Converter<Money, Double>() {
			@Override
			public Double convert(final Money m) {
				return Double.valueOf(m.getAmount());
			}
		});

		Arithmetics.registerOperation(Operator.ADDITION, Money.class, (left, right) -> new Money(left.getAmount() + right.getAmount()));
		Arithmetics.registerOperation(Operator.SUBTRACTION, Money.class, (left, right) -> new Money(left.getAmount() - right.getAmount()));
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Money.class, (left, right) -> new Money(left.getAmount() * right.getAmount()));
		Arithmetics.registerOperation(Operator.DIVISION, Money.class, (left, right) -> new Money(left.getAmount() / right.getAmount()));
		Arithmetics.registerDifference(Money.class, (left, right) -> {
			double result = Math.abs(left.getAmount() - right.getAmount());
			if (result < Skript.EPSILON)
				return new Money(0);
			return new Money(result);
		});
		Arithmetics.registerDefaultValue(Money.class, () -> new Money(0));
	}
	
	final double amount;
	
	public Money(final double amount) {
		this.amount = amount;
	}
	
	public double getAmount() {
		return amount;
	}
	
	@SuppressWarnings({"null", "unused"})
	@Nullable
	public static Money parse(final String s) {
		if (VaultHook.economy == null)
			return null;

		String singular = VaultHook.economy.currencyNameSingular(), plural = VaultHook.economy.currencyNamePlural();
		if (plural != null && !plural.isEmpty()) {
			Money money = parseMoney(s, plural);
			if (money != null)
				return money;
		}
		if (singular != null && !singular.isEmpty()) {
			return parseMoney(s, singular);
		}
		return null;
	}

	@Nullable
	private static Money parseMoney(String s, String addition) {
		if (StringUtils.endsWithIgnoreCase(s, addition)) {
			Double d = parseDouble(s.substring(0, s.length() - addition.length()).trim());
			if (d != null)
				return new Money(d);
		} else if (StringUtils.startsWithIgnoreCase(s, addition)) {
			Double d = parseDouble(s.substring(addition.length()).trim());
			if (d != null)
				return new Money(d);
		}
		return null;
	}

	@Nullable
	private static Double parseDouble(String s) {
		if (!JavaClasses.NUMBER_PATTERN.matcher(s).matches())
			return null;
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "" + VaultHook.economy.format(amount);
	}
	
}
