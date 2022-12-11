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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Converter.ConverterInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Loop Value")
@Description("The value currently being looped.")
@Examples({
	"# Countdown",
	"loop 10 times:",
	"\tmessage \"%11 - loop-number%\"",
	"\twait a second",
	"# Generate a 10x10 floor made of randomly coloured wool below the player",
	"loop blocks from the block below the player to the block 10 east of the block below the player:",
	"\tloop blocks from the loop-block to the block 10 north of the loop-block:",
	"\t\tset loop-block-2 to any wool"
})
@Since("1.0")
public class ExprLoopValue extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprLoopValue.class, Object.class, ExpressionType.SIMPLE,
			"[the] loop-<.+>"
		);
	}

	private static final Pattern LOOP_VALUE_NAME_PATTERN = Pattern.compile("^(.+)-(\\d+)$");
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private String name;
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private SecLoop loop;
	
	// Whether we are looping a variable
	private boolean isVariableLoop = false;
	// Whether we should return the index of the variable instead of the value
	private boolean isIndex = false;
	
	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		name = parser.expr;

		String type = parser.regexes.get(0).group();

		int loopValue = -1;
		Matcher matcher = LOOP_VALUE_NAME_PATTERN.matcher(type);
		if (matcher.matches()) {
			type = matcher.group(1);
			loopValue = Utils.parseInt("" + matcher.group(2));
		}
		Class<?> typeAsClass = Classes.getClassFromUserInput(type);

		int currentLoopValue = 1;
		SecLoop loop = null;

		for (SecLoop secLoop : getParser().getCurrentSections(SecLoop.class)) {

			Expression<?> loopedExpression = secLoop.getLoopedExpression();

			if (
				(typeAsClass != null && typeAsClass.isAssignableFrom(loopedExpression.getReturnType())) // loop-<type> (ex: loop-integer)
				|| type.equalsIgnoreCase("value") // loop-value
				|| isLoopOf(loopedExpression, type) // loop-<something> (ex: loop-argument)
			) {
				if (currentLoopValue < loopValue) { // Move onto the next one (ex: this is 'loop-value-1', but we want 'loop-value-2')
					currentLoopValue++;
					continue;
				}

				if (loop != null) { // The user was not specific enough - we can't determine which to use
					Skript.error(
						"There are multiple loops that match loop-" + type + "."
						+ " Use loop-" + type + "-1/2/3/etc. to specify which loop's value you want."
					);
					return false;
				}

				loop = secLoop;
				if (currentLoopValue == loopValue) // We found the right one
					break;
			}

		}

		if (loop == null) {
			Skript.error("There's no loop that matches 'loop-" + parser.regexes.get(0).group() + "'");
			return false;
		}

		if (loop.getLoopedExpression() instanceof Variable) {
			isVariableLoop = true;
			if (((Variable<?>) loop.getLoopedExpression()).isIndexLoop(type))
				isIndex = true;
		}

		this.loop = loop;

		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	protected <R> ConvertedExpression<Object, ? extends R> getConvertedExpr(Class<R>... to) {
		if (isVariableLoop && !isIndex) {
			Class<R> superType = (Class<R>) Utils.getSuperType(to);
			return new ConvertedExpression<>(this, superType,
				new ConverterInfo<>(Object.class, superType, o -> Converters.convert(o, to), 0)
			);
		} else {
			return super.getConvertedExpr(to);
		}
	}
	
	@Override
	public Class<?> getReturnType() {
		if (isIndex)
			return String.class;
		return loop.getLoopedExpression().getReturnType();
	}
	
	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	protected Object[] get(Event e) {
		if (isVariableLoop) {
			Entry<String, Object> current = (Entry<String, Object>) loop.getCurrent(e);

			if (current == null)
				return new Object[0];

			if (isIndex)
				return new String[]{current.getKey()};

			Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
			one[0] = current.getValue();
			return one;
		}

		Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
		one[0] = loop.getCurrent(e);
		return one;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String toString(@Nullable Event e, boolean debug) {
		if (e == null)
			return name;

		if (isVariableLoop) {
			Entry<String, Object> current = (Entry<String, Object>) loop.getCurrent(e);
			if (current == null)
				return Classes.getDebugMessage(null);
			return isIndex ? "\"" + current.getKey() + "\"" : Classes.getDebugMessage(current.getValue());
		}

		return Classes.getDebugMessage(loop.getCurrent(e));
	}

	private static final Map<Class<? extends Expression<?>>, LoopValueHandler<?>> LOOP_VALUE_HANDLERS = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <Type extends Expression<?>> boolean isLoopOf(Type source, String type) {
		//noinspection deprecation
		if (source.isLoopOf(type)) // for backwards compatibility
			return true;
		LoopValueHandler<Type> loopValueHandler = (LoopValueHandler<Type>) LOOP_VALUE_HANDLERS.get(source.getClass());
		if (loopValueHandler == null)
			return false;
		return loopValueHandler.isLoopOf(source, type);
	}

	public static <Type extends Expression<?>> void registerLoopValueHandler(Class<Type> source, LoopValueHandler<Type> loopValueHandler) {
		if (LOOP_VALUE_HANDLERS.containsKey(source))
			throw new SkriptAPIException("A loop value handler is already registered for: " + source);
		LOOP_VALUE_HANDLERS.put(source, loopValueHandler);
	}

	@FunctionalInterface
	public interface LoopValueHandler<T> {

		boolean isLoopOf(T source, String type);

	}
	
}
