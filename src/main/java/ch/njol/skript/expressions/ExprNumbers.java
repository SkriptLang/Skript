package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Numbers")
@Description({"All numbers between two given numbers, useful for looping.",
		"Use 'numbers' if your start is not an integer and you want to keep the fractional part of the start number constant, or use 'integers' if you only want to loop integers.",
		"You may also use 'decimals' if you want to use the decimal precision of the start number.",
		"You may want to use the 'times' expression instead, for instance 'loop 5 times:'"})
@Examples({"loop numbers from 2.5 to 5.5: # loops 2.5, 3.5, 4.5, 5.5",
		"loop integers from 2.9 to 5.1: # same as '3 to 5', i.e. loops 3, 4, 5",
		"loop decimals from 3.94 to 4: # loops 3.94, 3.95, 3.96, 3.97, 3.98, 3.99, 4"})
@Since("1.4.6 (integers & numbers), 2.5.1 (decimals)")
public class ExprNumbers extends SimpleExpression<Number> {
	static {
		Skript.registerExpression(ExprNumbers.class, Number.class, ExpressionType.COMBINED,
				"[(all [[of] the]|the)] (numbers|1¦integers|2¦decimals) (between|from) %number% (and|to) %number%");
	}
	
	@SuppressWarnings("null")
	private Expression<Number> start, end;
	private int mode;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		start = (Expression<Number>) exprs[0];
		end = (Expression<Number>) exprs[1];
		mode = parseResult.mark;
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(final Event event) {
		Number s = start.getSingle(event), f = end.getSingle(event);
		if (s == null || f == null)
			return null;
		final boolean reverse = s.doubleValue() > f.doubleValue();
		if (reverse) {
			Number temp = s;
			s = f;
			f = temp;
		}
		
		final List<Number> list = new ArrayList<>();
		if (mode == 0) {
			final double amount = Math.floor(f.doubleValue() - s.doubleValue() + 1);
			
			for (int i = 0; i < amount; i++) {
				list.add(s.doubleValue() + i);
			}
		} else if (mode == 1) {
			final double amount = Math.floor(f.doubleValue()) - Math.ceil(s.doubleValue()) + 1;
			final double low = Math.ceil(s.doubleValue());
			for (int i = 0; i < amount; i++) {
				list.add((long) low + i);
			}
		} else if (mode == 2) {
			
			final String[] split = (reverse ? f : s).toString().split("\\.");
			final int numberAccuracy = SkriptConfig.numberAccuracy.value();
			int precision = Math.min(split.length > 1 ? split[1].length() : 0, numberAccuracy);
			
			final double multiplier = Math.pow(10, precision);
			for (int i = (int) Math.ceil(s.doubleValue() * multiplier); i <= Math.floor(f.doubleValue() * multiplier); i++) {
				list.add((i / multiplier));
			}
		}

		if (reverse) Collections.reverse(list);
		return list.toArray(new Number[0]);
	}
	
	@Override
	@Nullable
	public Iterator<Number> iterator(final Event event) {
		Number s = start.getSingle(event), f = end.getSingle(event);
		if (s == null || f == null)
			return null;
		final boolean reverse = s.doubleValue() > f.doubleValue();
		if (reverse) {
			Number temp = s;
			s = f;
			f = temp;
		}
		final Number starting = s, finish = f;
		if (mode < 2) {
			return new Iterator<Number>() {
				double i = mode == 1 ? Math.ceil(starting.doubleValue()) : starting.doubleValue();
				double max = mode == 1 ? Math.floor(finish.doubleValue()) : finish.doubleValue();
				
				@Override
				public boolean hasNext() {
					return i <= max;
				}
				
				@Override
				public Number next() {
					if (!hasNext())
						throw new NoSuchElementException();
					if (mode == 1)
						return (long) (reverse ? max-- : i++);
					else
						return reverse ? max-- : i++;
				}
			};
		} else {
			return new Iterator<Number>() {
				final double min = starting.doubleValue();
				final double max = finish.doubleValue();
				
				final String[] split = (reverse ? finish : starting).toString().split("\\.");
				final int numberAccuracy = SkriptConfig.numberAccuracy.value();
				final int precision = Math.min(split.length > 1 ? split[1].length() : 0, numberAccuracy);
				final double multiplier = Math.pow(10, precision);
				
				final int intMax = (int) Math.floor(max * multiplier);
				final int intMin = (int) Math.ceil (min * multiplier);
				int current = reverse ? intMax : intMin;
				
				@Override
				public boolean hasNext() {
					return reverse ? (current >= intMin) : (current <= intMax);
				}
				
				@Override
				public Number next() {
					if (!hasNext())
						throw new NoSuchElementException();
					double value = current / multiplier;
					current += reverse ? -1 : 1;
					return value;
				}
			};
		}
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return mode == 1 && (s.equalsIgnoreCase("integer") || s.equalsIgnoreCase("int"));
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return mode == 1 ? Long.class : Double.class;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final String modeString = mode == 0 ? "numbers" : (mode == 1 ? "integers" : "decimals");
		return modeString + " from " + start.toString(e, debug) + " to " + end.toString(e, debug);
	}
	
}
