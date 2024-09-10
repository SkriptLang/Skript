package ch.njol.skript.effects;

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
		Skript.registerEffect(EffCancelEvent.class, "[:un]cancel [the] event");
	}
	
	private boolean cancel;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult result) {
		if (isDelayed == Kleenean.TRUE) {
			Skript.error("Can't cancel an event after it has already passed", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}

		cancel = !result.hasTag("un");
		Class<? extends Event>[] currentEvents = getParser().getCurrentEvents();

		if (currentEvents == null)
			return false;

		for (Class<? extends Event> e : currentEvents) {
			if (Cancellable.class.isAssignableFrom(e) || BlockCanBuildEvent.class.isAssignableFrom(e))
				return true; // TODO warning if some event(s) cannot be cancelled even though some can (needs a way to be suppressed)
		}

		if (getParser().isCurrentEvent(PlayerLoginEvent.class)) {
			Skript.error("A connect event cannot be cancelled, but the player may be kicked ('kick player by reason of \"...\"')", ErrorQuality.SEMANTIC_ERROR);
		} else if (getParser().isCurrentEvent(EntityToggleSwimEvent.class)) {
			Skript.error("Cancelling a toggle swim event has no effect", ErrorQuality.SEMANTIC_ERROR);
		} else {
			Skript.error(Utils.A(getParser().getCurrentEventName()) + " event cannot be cancelled", ErrorQuality.SEMANTIC_ERROR);
		}
		return false;
	}
	
	@Override
	public void execute(Event event) {
		if (event instanceof Cancellable)
			((Cancellable) event).setCancelled(cancel);
		if (event instanceof PlayerInteractEvent) {
			EvtClick.interactTracker.eventModified((Cancellable) event);
			((PlayerInteractEvent) event).setUseItemInHand(cancel ? Result.DENY : Result.DEFAULT);
			((PlayerInteractEvent) event).setUseInteractedBlock(cancel ? Result.DENY : Result.DEFAULT);
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
