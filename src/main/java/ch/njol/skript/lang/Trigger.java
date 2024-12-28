package ch.njol.skript.lang;

import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.List;

public class Trigger extends TriggerSection {

	private final String name;
	private final SkriptEvent event;

	private final @Nullable Script script;
	private int line = -1; // -1 is default: it means there is no line number available
	private String debugLabel;

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

}
