package ch.njol.skript.lang;

import ch.njol.skript.effects.Delay;
import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public class Trigger extends TriggerSection {

	private final String name;
	private final SkriptEvent event;

	private final @Nullable Script script;
	private int line = -1; // -1 is default: it means there is no line number available
	private String debugLabel;

	private final Set<Event> delayedEvents =
		Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	public Trigger(@Nullable Script script, String name, SkriptEvent event, List<TriggerItem> items) {
		super(items);
		this.script = script;
		this.name = name;
		this.event = event;
		this.debugLabel = "unknown trigger";
	}

	/**
	 * Executes this trigger for a certain event.
	 * @param event The event to execute this Trigger with.
	 * @return false if an exception occurred.
	 */
	public boolean execute(Event event) {
		boolean success = TriggerItem.walk(this, event);

		// Clear local variables
		Variables.removeLocals(event);
		/*
		 * Local variables can be used in delayed effects by backing reference
		 * of VariablesMap up. Basically:
		 *
		 * Object localVars = Variables.removeLocals(event);
		 *
		 * ... and when you want to continue execution:
		 *
		 * Variables.setLocalVariables(event, localVars);
		 *
		 * See Delay effect for reference.
		 */

		return success;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return walk(event, true);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return name + " (" + this.event.toString(event, debug) + ")";
	}

	/**
	 * @return The name of this trigger.
	 */
	public String getName() {
		return name;
	}

	public SkriptEvent getEvent() {
		return event;
	}

	/**
	 * @return The script this trigger was created from.
	 */
	public @Nullable Script getScript() {
		return script;
	}

	/**
	 * Sets line number for this trigger's start.
	 * Only used for debugging.
	 * @param line Line number
	 */
	public void setLineNumber(int line) {
		this.line  = line;
	}

	/**
	 * @return The line number where this trigger starts. This should ONLY be used for debugging!
	 */
	public int getLineNumber() {
		return line;
	}

	public void setDebugLabel(String label) {
		this.debugLabel = label;
	}

	public String getDebugLabel() {
		return debugLabel;
	}

	/**
	 * @return Whether the execution of this trigger for this specific event has been delayed.
	 */
	public boolean isExecutionDelayed(Event event) {
		return delayedEvents.contains(event);
	}

	/**
	 * Marks the execution of this trigger for this specific event as delayed.
	 */
	public void markExecutionAsDelayed(Event event) {
		delayedEvents.add(event);
		// for backwards compatibility
		//noinspection removal
		Delay.addDelayedEvent(event);
	}

}
