package ch.njol.skript.bukkitutil;

import ch.njol.skript.effects.EffCancelEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Tracks click events to remove extraneous events for one player click.
 */
public class ClickEventTracker {
	
	private static class TrackedEvent {
		
		/**
		 * The actual event that is tracked.
		 */
		final Cancellable event;
		
		/**
		 * Hand used in event.
		 */
		@SuppressWarnings("unused")
		final EquipmentSlot hand;

		public TrackedEvent(Cancellable event, EquipmentSlot hand) {
			this.event = event;
			this.hand = hand;
		}
		
	}
	
	/**
	 * First events by players during this tick. They're stored by their UUIDs.
	 * This map is cleared once per tick.
	 */
	final Map<UUID, TrackedEvent> firstEvents;
	
	/**
	 * Events that have been cancelled with {@link EffCancelEvent}.
	 */
	private final Set<Cancellable> modifiedEvents;
	
	public ClickEventTracker(JavaPlugin plugin) {
		this.firstEvents = new HashMap<>();
		this.modifiedEvents = new HashSet<>();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
				() -> {
					firstEvents.clear();
					modifiedEvents.clear();
				}, 1, 1);
	}
	
	/**
	 * Processes a click event from a player.
	 * @param player Player who caused it.
	 * @param event The event.
	 * @param hand Slot associated with the event.
	 * @return If the event should be passed to scripts.
	 */
	public boolean checkEvent(Player player, Cancellable event, EquipmentSlot hand) {
		UUID uuid = player.getUniqueId();
		TrackedEvent first = firstEvents.get(uuid);
		if (first != null && first.event != event) { // We've checked an event before, and it is not this one
			if (!modifiedEvents.contains(first.event)) {
				// Do not modify cancellation status of event, Skript did not touch it
				// This avoids issues like #2389
				return false;
			}
			
			// Ignore this, but set its cancelled status based on one set to first event
			if (event instanceof PlayerInteractEvent current) { // Handle use item/block separately
				// Failing to do so caused issue SkriptLang/Skript#2303
				Cancellable previous = first.event;
				if (previous instanceof PlayerInteractEvent prevClick) {
					current.setUseInteractedBlock(prevClick.useInteractedBlock());
					current.setUseItemInHand(prevClick.useItemInHand());
				} else {
					// in case the prev was PlayerInteractEntityEvent
					Event.Result newResult = previous.isCancelled() ? Event.Result.DENY : Event.Result.DEFAULT;
					current.setUseInteractedBlock(newResult);
					current.setUseItemInHand(newResult);
				}
			} else {
				event.setCancelled(first.event.isCancelled());
			}
			return false;
		} else { // Remember and run this
			firstEvents.put(uuid, new TrackedEvent(event, hand));
			return true;
		}
	}
	
	/**
	 * Records that given event was cancelled or uncancelled.
	 * @param event The event.
	 */
	public void eventModified(Cancellable event) {
		modifiedEvents.add(event);
	}
}
