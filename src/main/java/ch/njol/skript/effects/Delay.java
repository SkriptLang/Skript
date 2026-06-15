package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

@Name("Delay")
@Description({
	"Delays the script's execution by a given timespan.",
	"When used as an effect, all code after the <code>wait</code> runs once the delay elapses. The whole trigger pauses until the wait finishes.",
	"When used as a section, only the code within the section is deferred. Code after the section continues immediately, and the body runs on the same event once the delay elapses. This is useful for scheduling follow-up work without blocking the rest of the trigger.",
	"Note that delays are not persistent. For example, <code>ban player → wait 7 days → unban player</code> will not resume if the server restarts during the delay.",
	"Inside a section body, outer <code>loop-value</code>s are not available because the body is parsed as a separate trigger. Event values (like <code>player</code>) still work. If you need a value from before the delay, copy it to a local variable. Local variables are snapshotted when the section is scheduled, so subsequent changes aren't reflected."
})
@Example("wait 2 minutes")
@Example("halt for 5 minecraft hours")
@Example("wait a tick")
@Example("""
	# Section form: outer continues immediately, body runs 3 seconds later.
	send "hello" to player
	wait 3 seconds:
		send "...and goodbye" to player
	""")
@Example("""
	send "This runs first!" to player
	wait 1 seconds:
		send "This runs third! (one second later)" to player
	send "This runs second!" to player
	""")
@Since("1.4, INSERT VERSION (Delayed Sections)")
public class Delay extends EffectSection {

	static {
		Skript.registerSection(Delay.class, "(wait|halt) [for] %timespan%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	protected Expression<Timespan> duration;

	private @Nullable Trigger trigger;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
						@Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		duration = (Expression<Timespan>) exprs[0];
		if (duration instanceof Literal) { // If we can, do sanity check for delays
			Timespan timespan = ((Literal<Timespan>) duration).getSingle();
			if (timespan.isInfinite()) {
				Skript.error("Delaying for an eternity is not allowed. Use the 'stop' effect instead.");
				return false;
			}
			long millis = timespan.getAs(Timespan.TimePeriod.MILLISECOND);
			if (millis < 50) {
				Skript.warning("Delays less than one tick are not possible, defaulting to one tick.");
			}
		}

		if (hasSection()) {
			assert sectionNode != null;
			// Parse the body under the outer event context so event values still resolve inside it.
			// Type hints are propagated via SectionUtils; locals are isolated at runtime via the swap dance in walk().
			Class<? extends Event>[] outerEvents = getParser().getCurrentEvents();
			trigger = SectionUtils.loadDelayableLinkedCode("wait", (beforeLoading, afterLoading) -> {
				Runnable bodyBefore = () -> {
					beforeLoading.run();
					getParser().setHasDelayBefore(Kleenean.TRUE);
				};
				return loadCode(sectionNode, "wait", bodyBefore, afterLoading, outerEvents);
			});
			// Outer trigger is NOT delayed - code after the section runs immediately.
			return trigger != null;
		}

		getParser().setHasDelayBefore(Kleenean.TRUE);
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		debug(event, true);
		long start = Skript.debug() ? System.nanoTime() : 0;

		if (!Skript.getInstance().isEnabled()) { // See https://github.com/SkriptLang/Skript/issues/3702
			error("Cannot delay code execution while the server is shutting down. The delay will be ignored.");
			return trigger != null ? super.walk(event, false) : null;
		}

		Timespan duration = this.duration.getSingle(event);
		if (duration == null)
			return trigger != null ? super.walk(event, false) : null;

		long ticks = Math.max(duration.getAs(Timespan.TimePeriod.TICK), 1); // Minimum delay is one tick, less than it is useless!

		TriggerItem afterDelay = trigger != null ? trigger : getNext();
		if (afterDelay != null) {
			boolean isSection = trigger != null;
			Object localVars = isSection ? Variables.copyLocalVariables(event) : Variables.removeLocals(event);

			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
				addDelayedEvent(event);
				Skript.debug(getIndentation() + "... continuing after " + (System.nanoTime() - start) / 1_000_000_000. + "s");

				if (localVars != null)
					Variables.setLocalVariables(event, localVars);

				TriggerItem.walk(afterDelay, event);

				Variables.removeLocals(event); // Clean up local vars, we may be exiting now
			}, ticks);
		}

		if (trigger != null)
			return super.walk(event, false);
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "wait for " + duration.toString(event, debug) + (event == null ? "" : "...");
	}

	private static final Set<Event> DELAYED =
		Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	/**
	 * The main method for checking if the execution of {@link TriggerItem}s has been delayed.
	 * @param event The event to check for a delay.
	 * @return Whether {@link TriggerItem} execution has been delayed.
	 */
	public static boolean isDelayed(Event event) {
		return DELAYED.contains(event);
	}

	/**
	 * The main method for marking the execution of {@link TriggerItem}s as delayed.
	 * @param event The event to mark as delayed.
	 */
	public static void addDelayedEvent(Event event) {
		DELAYED.add(event);
	}

}