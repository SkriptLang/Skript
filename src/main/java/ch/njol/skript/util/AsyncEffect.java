package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Effects that extend this class are ran asynchronously. Next trigger item will be ran
 * in main server thread, as if there had been a delay before.
 * <p>
 * Majority of Skript and Minecraft APIs are not thread-safe, so be careful.
 * <p>
 * Make sure to add set {@link ch.njol.skript.lang.parser.ParserInstance#getHasDelayBefore()} to
 * {@link ch.njol.util.Kleenean#TRUE} in the {@code init} method.
 */
public abstract class AsyncEffect extends Effect {

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		debug(event, true);

		Delay.addDelayedEvent(event); // Mark this event as delayed
		Object localVars = Variables.removeLocals(event); // Back up local variables

		if (!Skript.getInstance().isEnabled()) // See https://github.com/SkriptLang/Skript/issues/3702
			return null;

		Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
			// Re-set local variables
			if (localVars != null)
				Variables.setLocalVariables(event, localVars);

			execute(event); // Execute this effect

			if (getNext() != null) {
				Bukkit.getScheduler().runTask(Skript.getInstance(), () -> { // Walk to next item synchronously
					Object timing = null;
					if (SkriptTimings.enabled()) { // getTrigger call is not free, do it only if we must
						Trigger trigger = getTrigger();
						if (trigger != null) {
							timing = SkriptTimings.start(trigger.getDebugLabel());
						}
					}

					TriggerItem.walk(getNext(), event);

					Variables.removeLocals(event); // Clean up local vars, we may be exiting now

					SkriptTimings.stop(timing); // Stop timing if it was even started
				});
			} else {
				Variables.removeLocals(event);
			}
		});

		return null;
	}
}
