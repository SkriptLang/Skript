package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.simplification.Simplifiable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.condition.Conditional;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * A condition which must be fulfilled for the trigger to continue. If the condition is in a section the behaviour depends on the section.
 */
public abstract class Condition extends Statement implements Conditional<Event>, SyntaxRuntimeErrorProducer, Simplifiable<Condition> {

	/**
	 * @deprecated This has been replaced by {@link Priority}.
	 * See the documentation of each element to determine their replacements.
	 */
	@Deprecated(since = "2.14", forRemoval = true)
	public enum ConditionType {

		/**
		 * Conditions that contain other expressions, e.g. "%properties% is/are within %expressions%"
		 * 
		 * @see #PROPERTY
		 * @deprecated Use {@link SyntaxInfo#COMBINED}.
		 */
		COMBINED(SyntaxInfo.COMBINED),

		/**
		 * Property conditions, e.g. "%properties% is/are data value[s]"
		 * @deprecated Use {@link PropertyCondition#DEFAULT_PRIORITY}.
		 */
		PROPERTY(PropertyCondition.DEFAULT_PRIORITY),

		/**
		 * Conditions whose pattern matches (almost) everything or should be last checked.
		 * @deprecated Use {@link SyntaxInfo#PATTERN_MATCHES_EVERYTHING}.
		 */
		PATTERN_MATCHES_EVERYTHING(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);

		private final Priority priority;

		ConditionType(Priority priority) {
			this.priority = priority;
		}

		/**
		 * @return The Priority equivalent of this ConditionType.
		 */
		public Priority priority() {
			return this.priority;
		}

	}

	private boolean negated;

	protected Condition() {}

	private Node node;

	@Override
	public boolean preInit() {
		node = getParser().getNode();
		return super.preInit();
	}

	/**
	 * Checks whether this condition is satisfied with the given event. This should not alter the event or the world in any way, as conditions are only checked until one returns
	 * false. All subsequent conditions of the same trigger will then be omitted.<br/>
	 * <br/>
	 * You might want to use {@link SimpleExpression#check(Event, Predicate)}
	 *
	 * @param event the event to check
	 * @return <code>true</code> if the condition is satisfied, <code>false</code> otherwise or if the condition doesn't apply to this event.
	 */
	public abstract boolean check(Event event);

	@Override
	public Kleenean evaluate(Event event) {
		return Kleenean.get(check(event));
	}

	@Override
	public final boolean run(Event event) {
		return check(event);
	}

	/**
	 * Sets the negation state of this condition. This will change the behaviour of {@link Expression#check(Event, Predicate, boolean)}.
	 */
	protected final void setNegated(boolean invert) {
		negated = invert;
	}

	/**
	 * @return whether this condition is negated or not.
	 */
	public final boolean isNegated() {
		return negated;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public @NotNull String getSyntaxTypeName() {
		return "condition";
	}

	@Override
	public Condition simplify() {
		return this;
	}

	/**
	 * Parse a raw string input as a condition.
	 * 
	 * @param input The string input to parse as a condition.
	 * @param defaultError The error if the condition fails.
	 * @return Condition if parsed correctly, otherwise null.
	 */
	public static @Nullable Condition parse(String input, @Nullable String defaultError) {
		input = input.trim();
		while (input.startsWith("(") && SkriptParser.next(input, 0, ParseContext.DEFAULT) == input.length())
			input = input.substring(1, input.length() - 1);
		var iterator = Skript.instance().syntaxRegistry().syntaxes(org.skriptlang.skript.registration.SyntaxRegistry.CONDITION).iterator();
		//noinspection unchecked,rawtypes
		return (Condition) SkriptParser.parse(input, (Iterator) iterator, defaultError);
	}

}
