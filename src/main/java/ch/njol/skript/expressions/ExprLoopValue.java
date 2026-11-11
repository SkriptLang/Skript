package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to access a loop's current value.
 */
@Name("Loop value")
@Description("Returns the previous, current, or next looped value.")
@Example("""
	# Countdown
	loop 10 times:
		message "%11 - loop-number%"
		wait a second
	""")
@Example("""
	# Generate a 10x10 floor made of randomly colored wool below the player
	loop blocks from the block below the player to the block 10 east of the block below the player:
		loop blocks from the loop-block to the block 10 north of the loop-block:
			set loop-block-2 to any wool
	""")
@Example("""
	loop {top-balances::*}:
		loop-iteration <= 10
		send "#%loop-iteration% %loop-index% has $%loop-value%"
	""")
@Example("""
	loop shuffled (integers between 0 and 8):
		if all:
			previous loop-value = 1
			loop-value = 4
			next loop-value = 8
		then:
			 kill all players
	""")
@Since("1.0, 2.8.0 (loop-counter), 2.10 (previous, next)")
public class ExprLoopValue extends SimpleExpression<Object> {

	enum LoopState {
		CURRENT("[current]"),
		NEXT("next"),
		PREVIOUS("previous");

		private final String pattern;

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

	private String name;

	private SecLoop loop;

	// whether this loops a keyed expression (e.g. a variable)
	boolean isKeyedLoop = false;
	// if this loops a variable and isIndex is true, return the index of the variable instead of the value
	boolean isIndex = false;

	private LoopState selectedState;

	private static final Pattern LOOP_PATTERN = Pattern.compile("^(.+)-(\\d+)$");

	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		selectedState = loopStates[matchedPattern];
		name = parser.expr;
		String loopOf = parser.regexes.get(0).group();
		int expectedDepth = -1;
		Matcher m = LOOP_PATTERN.matcher(loopOf);
		if (m.matches()) {
			loopOf = m.group(1);
			expectedDepth = Utils.parseInt(m.group(2));
		}

		if ("counter".equalsIgnoreCase(loopOf) || "iteration".equalsIgnoreCase(loopOf)) // ExprLoopIteration - in case of classinfo conflicts
			return false;

		Class<?> expectedClass = Classes.getClassFromUserInput(loopOf);
		int candidateDepth = 1;
		SecLoop loop = null;

		for (SecLoop candidate : getParser().getCurrentSections(SecLoop.class)) {
			if ((expectedClass != null && expectedClass.isAssignableFrom(candidate.getLoopedExpression().getReturnType()))
				|| "value".equalsIgnoreCase(loopOf)
				|| candidate.getLoopedExpression().isLoopOf(loopOf)
			) {
				if (candidateDepth < expectedDepth) {
					candidateDepth++;
					continue;
				}
				if (loop != null) {
					Skript.error("There are multiple loops that match loop-" + loopOf + ". Use loop-" + loopOf + "-1/2/3/etc. to specify which loop's value you want.");
					return false;
				}
				loop = candidate;
				if (candidateDepth == expectedDepth)
					break;
			}
		}
		if (loop == null) {
			Skript.error("There's no loop that matches 'loop-" + loopOf + "'");
			return false;
		}
		if (selectedState == LoopState.NEXT && !loop.supportsPeeking()) {
			Skript.error("The expression '" + loop.getExpression().toString() + "' does not allow the usage of 'next loop-" + loopOf + "'.");
			return false;
		}
		if (loop.isKeyedLoop()) {
			isKeyedLoop = true;
			if (((KeyedIterableExpression<?>) loop.getLoopedExpression()).isIndexLoop(loopOf))
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
	public Class<?> getReturnType() {
		if (isIndex)
			return String.class;
		return loop.getLoopedExpression().getReturnType();
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		if (isIndex)
			return new Class[]{String.class};
		return loop.getLoopedExpression().possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		if (isIndex)
			return super.canReturn(returnType);
		return loop.getLoopedExpression().canReturn(returnType);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (isKeyedLoop) {
			//noinspection unchecked
			KeyedValue<Object> value = (KeyedValue<Object>) switch (selectedState) {
				case CURRENT ->  loop.getCurrent(event);
				case NEXT -> loop.getNext(event);
				case PREVIOUS -> loop.getPrevious(event);
			};
			if (value == null)
				return null;
			if (isIndex)
				return new String[] {value.key()};
			Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
			one[0] = value.value();
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
		if (isKeyedLoop) {
			//noinspection unchecked
			KeyedValue<Object> value = (KeyedValue<Object>) switch (selectedState) {
				case CURRENT ->  loop.getCurrent(event);
				case NEXT -> loop.getNext(event);
				case PREVIOUS -> loop.getPrevious(event);
			};
			if (value == null)
				return Classes.getDebugMessage(null);
			return isIndex ? "\"" + value.key() + "\"" : Classes.getDebugMessage(value.value());
		}
		return Classes.getDebugMessage(switch (selectedState) {
			case CURRENT -> loop.getCurrent(event);
			case NEXT -> loop.getNext(event);
			case PREVIOUS -> loop.getPrevious(event);
		});
	}

}
