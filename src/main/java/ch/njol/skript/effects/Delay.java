package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

@Name("Delay")
@Description("Delays the script's execution by a given timespan. Please note that delays are not persistent, e.g. " +
	"trying to create a tempban script with <code>ban player → wait 7 days → unban player</code> will not work if you " +
	"restart your server anytime within these 7 days. You also have to be careful even when using small delays!")
@Examples({
	"wait 2 minutes",
	"halt for 5 minecraft hours",
	"wait a tick"
})
@Since("1.4, INSERT VERSION (tasks: experimental)")
// todo doc
public class Delay extends Effect {

	static {
		Skript.registerEffect(Delay.class,
			"wait %timespan% for %timespan/task%",
			"wait for %timespan/task%",
			"(wait|halt) [for] %timespan%"
		);
	}

	protected Expression<?> target;
	protected @Nullable Expression<Timespan> delay;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		this.getParser().setHasDelayBefore(Kleenean.TRUE);

		if (pattern == 1) {
			//noinspection unchecked
			this.delay = (Expression<Timespan>) expressions[0];
			this.target = expressions[1];
		} else {
			this.target = expressions[0];
		}
		if (target instanceof Literal<?> literal && literal.getSingle() instanceof Timespan duration) {
			// If we can, do sanity check for delays
			long millis = duration.getAs(Timespan.TimePeriod.MILLISECOND);
			if (millis < 50)
				Skript.warning("Delays less than one tick are not possible, defaulting to one tick.");
		}

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		Object single = this.target.getSingle(event);
		debug(event, true);
		if (this.getNext() == null || !Skript.getInstance().isEnabled())
			return null;
		addDelayedEvent(event);
		if (single instanceof Timespan timespan)
			return this.walk(event, timespan);
		if (single instanceof Task task)
			return this.walk(event, task);
		return super.walk(event);
	} // todo note breaking change: wait for <none> will now not wait, before it would just kill the trigger

	protected @Nullable TriggerItem walk(Event event, Timespan duration) {
		TriggerItem next = getNext();
		long start = Skript.debug() ? System.nanoTime() : 0;

		// Back up local variables
		Object variables = Variables.removeLocals(event);

		Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(),
			() -> reschedule(start, variables, next, event),
			Math.max(duration.getAs(Timespan.TimePeriod.TICK), 1)); // Minimum delay is one tick
		return null;
	}

	protected @Nullable TriggerItem walk(Event event, Task task) {
		Timespan timeout = delay != null ? delay.getSingle(event) : null;

		TriggerItem next = getNext();
		long start = Skript.debug() ? System.nanoTime() : 0;

		// Back up local variables
		Object variables = Variables.removeLocals(event);

		Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
			if (timeout != null) {
				Duration duration = timeout.getDuration();
				task.await(duration.get(ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
			} else {
				task.await();
			}
			Bukkit.getScheduler().runTask(Skript.getInstance(),
				() -> reschedule(start, variables, next, event));
		});
		return null;
	}

	protected void reschedule(long start, Object variables, TriggerItem next, Event event) {
		Skript.debug(getIndentation() + "... continuing after " + (System.nanoTime() - start) / 1_000_000_000. + "s");

		// Re-set local variables
		if (variables != null)
			Variables.setLocalVariables(event, variables);

		Object timing = null; // Timings reference must be kept so that it can be stopped after TriggerItem execution
		if (SkriptTimings.enabled()) { // getTrigger call is not free, do it only if we must
			Trigger trigger = getTrigger();
			if (trigger != null)
				timing = SkriptTimings.start(trigger.getDebugLabel());
		}

		TriggerItem.walk(next, event);
		Variables.removeLocals(event); // Clean up local vars, we may be exiting now

		SkriptTimings.stop(timing); // Stop timing if it was even started
	}

	@Override
	protected void execute(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (delay != null)
			return "wait " + delay.toString(event, debug) + " for " + target.toString(event, debug);
		return "wait for " + target.toString(event, debug);
	}

	private static final Set<Event> DELAYED =
		Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	/**
	 * The main method for checking if the execution of {@link TriggerItem}s has been delayed.
	 *
	 * @param event The event to check for a delay.
	 * @return Whether {@link TriggerItem} execution has been delayed.
	 */
	public static boolean isDelayed(Event event) {
		return DELAYED.contains(event);
	}

	/**
	 * The main method for marking the execution of {@link TriggerItem}s as delayed.
	 *
	 * @param event The event to mark as delayed.
	 */
	public static void addDelayedEvent(Event event) {
		DELAYED.add(event);
	}

}
