package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.EvtClick;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.Nullable;

@Name("Cancel Event")
@Description("Cancels the event (e.g. prevent blocks from being placed, or damage being taken).")
@Examples({
	"on damage:",
		"\tvictim is a player",
		"\tvictim has the permission \"skript.god\"",
		"\tcancel the event"
})
@Since("1.0")
public class EffCancelEvent extends Effect {

	static {
		Skript.registerEffect(EffCancelEvent.class, "cancel [the] event", "uncancel [the] event");
	}
	
	private boolean cancel;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (isDelayed == Kleenean.TRUE) {
			Skript.error("An event cannot be cancelled after it has already passed", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}

		cancel = matchedPattern == 0;
		Class<? extends Event>[] currentEvents = getParser().getCurrentEvents();

		if (currentEvents == null)
			return false;

		if (cancel && getParser().isCurrentEvent(EntityToggleSwimEvent.class)) {
			Skript.error("Cancelling a toggle swim event has no effect");
			return false;
		}

		for (Class<? extends Event> event : currentEvents) {
			if (Cancellable.class.isAssignableFrom(event) || BlockCanBuildEvent.class.isAssignableFrom(event))
				return true; // TODO warning if some event(s) cannot be cancelled even though some can (needs a way to be suppressed)
		}

		if (getParser().isCurrentEvent(PlayerLoginEvent.class))
			Skript.error("A connect event cannot be cancelled, but the player may be kicked ('kick player by reason of \"...\"')", ErrorQuality.SEMANTIC_ERROR);
		else
			Skript.error(Utils.A(getParser().getCurrentEventName()) + " event cannot be cancelled", ErrorQuality.SEMANTIC_ERROR);
		return false;
	}
	
	@Override
	public void execute(Event event) {
		if (event instanceof Cancellable)
			((Cancellable) event).setCancelled(cancel);
		if (event instanceof PlayerInteractEvent) {
			EvtClick.interactTracker.eventModified((Cancellable) event);
			((PlayerInteractEvent) event).setUseItemInHand(cancel ? Event.Result.DENY : Event.Result.DEFAULT);
			((PlayerInteractEvent) event).setUseInteractedBlock(cancel ? Event.Result.DENY : Event.Result.DEFAULT);
		} else if (event instanceof BlockCanBuildEvent) {
			((BlockCanBuildEvent) event).setBuildable(!cancel);
		} else if (event instanceof PlayerDropItemEvent) {
			PlayerUtils.updateInventory(((PlayerDropItemEvent) event).getPlayer());
		} else if (event instanceof InventoryInteractEvent) {
			PlayerUtils.updateInventory(((Player) ((InventoryInteractEvent) event).getWhoClicked()));
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (cancel ? "" : "un") + "cancel event";
	}
	
}
