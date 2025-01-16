package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
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
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to access a loop's current value.
 */
@Name("Loop value")
@Description("Returns the previous, current, or next looped value.")
@Examples({
	"# Countdown",
	"loop 10 times:",
		"\tmessage \"%11 - loop-number%\"",
		"\twait a second",
	"",
	"# Generate a 10x10 floor made of randomly colored wool below the player",
	"loop blocks from the block below the player to the block 10 east of the block below the player:",
		"\tloop blocks from the loop-block to the block 10 north of the loop-block:",
			"\t\tset loop-block-2 to any wool",
	"",
	"loop {top-balances::*}:",
		"\tloop-iteration <= 10",
		"\tsend \"#%loop-iteration% %loop-index% has $%loop-value%\"",
	"",
	"loop shuffled (integers between 0 and 8):",
		"\tif all:",
			"\t\tprevious loop-value = 1",
			"\t\tloop-value = 4",
			"\t\tnext loop-value = 8",
		"\tthen:",
			"\t\t kill all players"
})
@Since("1.0, 2.8.0 (loop-counter), 2.10 (previous, next)")
public class ExprLoopValue extends SimpleExpression<Object> {

	enum LoopState {
		CURRENT("[current]"),
		NEXT("next"),
		PREVIOUS("previous");

		private String pattern;

		LoopState(String pattern) {
			this.pattern = pattern;
		}
	}

	private static final LoopState[] loopStates = LoopState.values();

	static {
		String[] patterns = new String[loopStates.length];
		for (LoopState state : loopStates) {
			patterns[state.ordinal()] = "[the] " + state.pattern + " loop-<.+>";
		}
		Skript.registerExpression(ExprLoopValue.class, Object.class, ExpressionType.SIMPLE, patterns);
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private String name;
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private SecLoop loop;
	
	// whether this loops a variable
	boolean isVariableLoop = false;
	// if this loops a variable and isIndex is true, return the index of the variable instead of the value
	boolean isIndex = false;

	private LoopState selectedState;

	private static final Pattern LOOP_PATTERN = Pattern.compile("^(.+)-(\\d+)$");

	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		selectedState = loopStates[matchedPattern];
		name = parser.expr;
		String s = "" + parser.regexes.get(0).group();
		int i = -1;
		Matcher m = LOOP_PATTERN.matcher(s);
		if (m.matches()) {
			s = "" + m.group(1);
			i = Utils.parseInt("" + m.group(2));
		}

		if ("counter".equalsIgnoreCase(s) || "iteration".equalsIgnoreCase(s)) // ExprLoopIteration - in case of classinfo conflicts
			return false;

		Class<?> c = Classes.getClassFromUserInput(s);
		int j = 1;
		SecLoop loop = null;

		for (SecLoop l : getParser().getCurrentSections(SecLoop.class)) {
			if ((c != null && c.isAssignableFrom(l.getLoopedExpression().getReturnType())) || "value".equalsIgnoreCase(s) || l.getLoopedExpression().isLoopOf(s)) {
				if (j < i) {
					j++;
					continue;
				}
				if (loop != null) {
					Skript.error("There are multiple loops that match loop-" + s + ". Use loop-" + s + "-1/2/3/etc. to specify which loop's value you want.");
					return false;
				}
				loop = l;
				if (j == i)
					break;
			}
		}
		if (loop == null) {
			Skript.error("There's no loop that matches 'loop-" + s + "'");
			return false;
		}
		if (selectedState == LoopState.NEXT && !loop.supportsPeeking()) {
			Skript.error("The expression '" + loop.getExpression().toString() + "' does not allow the usage of 'next loop-" + s + "'.");
			return false;
		}
		if (loop.getLoopedExpression() instanceof Variable) {
			isVariableLoop = true;
			if (((Variable<?>) loop.getLoopedExpression()).isIndexLoop(s))
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
					new ConverterInfo<>(Object.class, superType, new Converter<Object, R>() {
				@Override
				@Nullable
				public R convert(Object o) {
					return Converters.convert(o, to);
				}
			}, 0));
		} else {
			return super.getConvertedExpr(to);
		}
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		if (isIndex)
			return String.class;
		return loop.getLoopedExpression().getReturnType();
	}
	
	@Override
	protected Object @Nullable [] get(Event event) {
		if (isVariableLoop) {
			//noinspection unchecked
			Entry<String, Object> value = (Entry<String, Object>) switch (selectedState) {
				case CURRENT ->  loop.getCurrent(event);
				case NEXT -> loop.getNext(event);
				case PREVIOUS -> loop.getPrevious(event);
			};
			if (value == null)
				return null;
			if (isIndex)
				return new String[] {value.getKey()};
			Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
			one[0] = value.getValue();
			return one;
		}

		Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
		one[0] = switch (selectedState) {
			case CURRENT -> loop.getCurrent(event);
			case NEXT -> loop.getNext(event);
			case PREVIOUS -> loop.getPrevious(event);
		};
		return one;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (event == null)
			return name;
		if (isVariableLoop) {
			//noinspection unchecked
			Entry<String, Object> value = (Entry<String, Object>) switch (selectedState) {
				case CURRENT ->  loop.getCurrent(event);
				case NEXT -> loop.getNext(event);
				case PREVIOUS -> loop.getPrevious(event);
			};
			if (value == null)
				return Classes.getDebugMessage(null);
			return isIndex ? "\"" + value.getKey() + "\"" : Classes.getDebugMessage(value.getValue());
		}
		return Classes.getDebugMessage(switch (selectedState) {
			case CURRENT -> loop.getCurrent(event);
			case NEXT -> loop.getNext(event);
			case PREVIOUS -> loop.getPrevious(event);
		});
	}
	
}
