package org.skriptlang.skript.variables.storage;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.ApiStatus;

/**
 * At the end of every tick, applies reported changes left over from earlier in the tick, then runs the variable
 * refresh, each for as long as {@link TickBudget} allows.
 * This is the only part of either that needs the server, so the rest can be tested without one.
 */
@ApiStatus.Internal
public final class VariableRefreshTicker implements Listener {

	private final VariableStorageManager manager;
	// System.nanoTime() when the current tick started
	private long tickStart = TickBudget.UNKNOWN;

	/**
	 * @param manager the manager whose variables to refresh
	 */
	public VariableRefreshTicker(VariableStorageManager manager) {
		this.manager = manager;
	}

	@EventHandler
	public void onTickStart(ServerTickStartEvent event) {
		tickStart = System.nanoTime();
	}

	// last, so the time other listeners took is already used up
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTickEnd(ServerTickEndEvent event) {
		ServerTickManager ticks = Bukkit.getServerTickManager();
		long tickNanos = (long) (1e9 / ticks.getTickRate());
		boolean sprinting = ticks.isSprinting();
		long eventRemaining = event.getTimeRemaining();
		long start = System.nanoTime();
		manager.reportTick(start + TickBudget.reportBudget(tickStart, start, tickNanos, eventRemaining, sprinting));
		// the event's estimate was made before the reports ran, so take off the time they took
		long now = System.nanoTime();
		long budget = TickBudget.budget(tickStart, now, tickNanos, eventRemaining - (now - start), sprinting);
		manager.refreshTick(now, now + budget);
	}

}
