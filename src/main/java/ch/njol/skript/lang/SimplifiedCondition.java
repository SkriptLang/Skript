package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;

/**
 * Represents a condition that can be simplified during initialization.
 */
public class SimplifiedCondition extends Condition {

	/**
	 * Creates a new {@link SimplifiedCondition} from a {@link Condition} by evaluating it with a {@link ContextlessEvent}.
	 * Any expression used by {@code original} that requires specific event data cannot be safely simplified.
	 *
	 * @param original The original {@link Condition} to simplify.
	 * @return A new {@link SimplifiedCondition}.
	 */
	public static Condition fromCondition(Condition original) {
		return fromCondition(original, (!TestMode.ENABLED || TestMode.DEV_MODE));
	}

	/**
	 * Creates a new {@link SimplifiedCondition} from a {@link Condition} by evaluating it with a {@link ContextlessEvent}.
	 * Any expression used by {@code original} that requires specific event data cannot be safely simplified.
	 *
	 * @param original The original {@link Condition} to simplify.
	 * @param warn Whether a warning for constant conditions should be outputted.
	 * @return A new {@link SimplifiedCondition}.
	 */
	public static Condition fromCondition(Condition original, boolean warn) {
		if (original instanceof SimplifiedCondition simplifiedCondition)
			return simplifiedCondition;

		Event event = ContextlessEvent.get();
		boolean result = original.check(event);

		if (warn) {
			ParserInstance parser = ParserInstance.get();
			Script script = parser.isActive() ? parser.getCurrentScript() : null;
			if (script != null && !script.suppressesWarning(ScriptWarning.CONSTANT_CONDITION)) {
				Skript.warning("The condition '" + original.toString(event, Skript.debug()) + "' will always be " + (result ? "true" : "false") + ".");
			}
		}

		return new SimplifiedCondition(original, result);
	}

	private final Condition source;
	private final boolean result;

	/**
	 * Constructs a new {@link SimplifiedCondition}.
	 *
	 * @param source The source {@link Condition} this was created from.
	 * @param result The evaluated result from {@code source} via {@link Condition#check(Event)}.
	 */
	private SimplifiedCondition(Condition source, boolean result) {
		this.source = source;
		this.result = result;
	}

	/**
	 * Returns the source {@link Condition} used to create this {@link SimplifiedCondition}.
	 */
	public Condition getSource() {
		return source;
	}

	/**
	 * Returns the result that was evaluated from {@link #source} during initialization.
	 */
	public boolean getResult() {
		return result;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean check(Event event) {
		return result;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return source.toString(event, debug);
	}

}
