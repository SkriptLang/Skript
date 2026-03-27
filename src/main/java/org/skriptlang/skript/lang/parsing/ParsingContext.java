package org.skriptlang.skript.lang.parsing;

import ch.njol.skript.lang.parser.ParsingStack;
import ch.njol.skript.variables.HintManager;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.parsing.constraints.ConstraintStack;

import java.util.Arrays;
import java.util.function.Supplier;

public class ParsingContext {

	// Shared by reference (self-managing push/pop) — do not deep copy in fork()
	private ParsingStack parsingStack;
	private ConstraintStack constraintStack = new ConstraintStack();

	public ParsingStack getParsingStack() {
		return parsingStack;
	}

	public void setParsingStack(ParsingStack parsingStack) {
		this.parsingStack = parsingStack;
	}

	public ConstraintStack getConstraintStack() {
		return constraintStack;
	}

	public void setConstraintStack(ConstraintStack constraintStack) {
		this.constraintStack = constraintStack;
	}

	// Fields that init() can modify — branches need isolation
	@SuppressWarnings("unchecked")
	private Class<? extends Event>[] currentEvents = new Class[0];
	private @Nullable ExperimentSet experimentSet; // shared ref, doesn't change mid-session
	private Kleenean hasDelayBefore = Kleenean.FALSE;
	private HintManager hintManager = new HintManager(false);

	// -------------------------------------------------------------------------
	// Getters / setters
	// -------------------------------------------------------------------------

	public Class<? extends Event>[] getCurrentEvents() {
		return currentEvents;
	}

	public void setCurrentEvents(Class<? extends Event> @Nullable [] currentEvents) {
		this.currentEvents = currentEvents != null ? currentEvents : new Class[0];
	}

	@SafeVarargs
	public final boolean isCurrentEvent(Class<? extends Event>... events) {
		for (Class<? extends Event> event : events) {
			for (Class<? extends Event> currentEvent : currentEvents) {
				if (event.isAssignableFrom(currentEvent))
					return true;
			}
		}
		return false;
	}

	public @Nullable ExperimentSet getExperimentSet() {
		return experimentSet;
	}

	public void setExperimentSet(@Nullable ExperimentSet experimentSet) {
		this.experimentSet = experimentSet;
	}

	public Kleenean getHasDelayBefore() {
		return hasDelayBefore;
	}

	public void setHasDelayBefore(Kleenean hasDelayBefore) {
		this.hasDelayBefore = hasDelayBefore;
	}

	public HintManager getHintManager() {
		return hintManager;
	}

	public void setHintManager(HintManager hintManager) {
		this.hintManager = hintManager;
	}

	// -------------------------------------------------------------------------
	// fork() — creates an isolated copy for branch parsing
	// -------------------------------------------------------------------------

	/**
	 * Creates a fork of this context for isolated branch parsing (e.g. section bodies).
	 * The fork shares {@link #constraintStack} and {@link #parsingStack} by reference,
	 * but owns independent copies of the fields {@code init()} can modify.
	 */
	public ParsingContext fork() {
		ParsingContext copy = new ParsingContext();
		// Shared by reference — self-managing push/pop, changes visible to all forks
		copy.constraintStack = this.constraintStack;
		copy.parsingStack = this.parsingStack;
		// Independent copies — branches need isolation
		copy.currentEvents = Arrays.copyOf(this.currentEvents, this.currentEvents.length);
		copy.experimentSet = this.experimentSet; // shared ref (immutable object mid-session)
		copy.hasDelayBefore = this.hasDelayBefore;
		copy.hintManager = this.hintManager.copy();
		return copy;
	}

	/**
	 * Creates a fork with the given constraint stack and parsing stack.
	 * Used internally by {@code ParserInstance} when initialising a fresh context.
	 */
	public void initSharedRefs(ParsingStack parsingStack, ConstraintStack constraintStack) {
		this.parsingStack = parsingStack;
		this.constraintStack = constraintStack;
	}

	// -------------------------------------------------------------------------
	// attempt() — wraps init() calls to snapshot/restore mutable parse state
	// -------------------------------------------------------------------------

	/**
	 * Executes the given action, rolling back any changes to {@link #hasDelayBefore}
	 * and {@link #hintManager} if the action returns {@code null} (i.e. fails).
	 * This prevents a failed {@code init()} from leaving dirty parse state.
	 *
	 * @param action The parse action to attempt.
	 * @param <T> The result type.
	 * @return The result of the action, or {@code null} if it failed (state rolled back).
	 */
	public <T> @Nullable T attempt(Supplier<@Nullable T> action) {
		Kleenean savedDelay = this.hasDelayBefore;
		HintManager savedHints = hintManager.isActive() ? hintManager.copy() : hintManager;

		T result = action.get();

		if (result == null) {
			this.hasDelayBefore = savedDelay;
			this.hintManager = savedHints;
		}
		return result;
	}

}
