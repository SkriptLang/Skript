package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.sections.EffSecSpawn.SpawnEvent;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.Direction;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import io.papermc.lib.PaperLib;
import io.papermc.lib.environments.PaperEnvironment;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;

@Name("Teleport")
@Description({
	"Teleport an entity to a specific location. ",
	"This effect is delayed by default on Paper, meaning certain syntax such as the return effect for functions cannot be used after this effect.",
	"The keyword 'force' indicates this effect will not be delayed, ",
	"which may cause lag spikes or server crashes when using this effect to teleport entities to unloaded chunks."
})
@Examples({
	"teleport the player to {homes::%player%}",
	"teleport the attacker to the victim"
})
@Since("1.0")
public class EffTeleport extends Effect {

	private static final boolean CAN_RUN_ASYNC = PaperLib.getEnvironment() instanceof PaperEnvironment;

	static {
		Skript.registerEffect(EffTeleport.class, "[(1¦force)] teleport %entities% (to|%direction%) %location%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Entity> entities;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Location> location;

	private boolean isAsync;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		location = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		isAsync = CAN_RUN_ASYNC && parseResult.mark == 0;

		if (getParser().isCurrentEvent(SpawnEvent.class)) {
			Skript.error("You cannot be teleporting an entity that hasn't spawned yet. Ensure you're using the location expression from the spawn section pattern.");
			return false;
		}

		if (isAsync)
			getParser().setHasDelayBefore(Kleenean.UNKNOWN); // UNKNOWN because it isn't async if the chunk is already loaded.
		return true;
	}

	@Nullable
	@Override
	protected TriggerItem walk(Event e) {
		debug(e, true);

		TriggerItem next = getNext();

		boolean delayed = Delay.isDelayed(e);
		
		Location loc = location.getSingle(e);
		if (loc == null)
			return next;
		boolean unknownWorld = !loc.isWorldLoaded();

		Entity[] entityArray = entities.getArray(e); // We have to fetch this before possible async execution to avoid async local variable access.
		if (entityArray.length == 0)
			return next;

		if (!delayed) {
			if (e instanceof PlayerRespawnEvent && entityArray.length == 1 && entityArray[0].equals(((PlayerRespawnEvent) e).getPlayer())) {
				if (unknownWorld)
					return next;
				((PlayerRespawnEvent) e).setRespawnLocation(loc);
				return next;
			}

			if (e instanceof PlayerMoveEvent && entityArray.length == 1 && entityArray[0].equals(((PlayerMoveEvent) e).getPlayer())) {
				if (unknownWorld) { // we can approximate the world
					loc = loc.clone();
					loc.setWorld(((PlayerMoveEvent) e).getFrom().getWorld());
				}
				((PlayerMoveEvent) e).setTo(loc);
				return next;
			}
		}

		if (!isAsync) {
			for (Entity entity : entityArray) {
				EntityUtils.teleport(entity, loc);
			}
			return next;
		}

		if (unknownWorld) { // we can't fetch the chunk without a world
			if (entityArray.length == 1) { // if there's 1 thing we can borrow its world
				Entity entity = entityArray[0];
				if (entity == null)
					return next;
				// assume it's a local teleport, use the first entity we find as a reference
				loc = loc.clone();
				loc.setWorld(entity.getWorld());
			} else {
				return next; // no entities = no chunk = nobody teleporting
			}
		}
		final Location fixed = loc;
		Delay.addDelayedEvent(e);
		Object localVars = Variables.removeLocals(e);
		
		// This will either fetch the chunk instantly if on Spigot or already loaded or fetch it async if on Paper.
		PaperLib.getChunkAtAsync(loc).thenAccept(chunk -> {
			// The following is now on the main thread
			for (Entity entity : entityArray) {
				EntityUtils.teleport(entity, fixed);
			}

			// Re-set local variables
			if (localVars != null)
				Variables.setLocalVariables(e, localVars);
			
			// Continue the rest of the trigger if there is one
			Object timing = null;
			if (next != null) {
				if (SkriptTimings.enabled()) {
					Trigger trigger = getTrigger();
					if (trigger != null) {
						timing = SkriptTimings.start(trigger.getDebugLabel());
					}
				}

				TriggerItem.walk(next, e);
			}
			Variables.removeLocals(e); // Clean up local vars, we may be exiting now
			SkriptTimings.stop(timing);
		});
		return null;
	}

	@Override
	protected void execute(Event e) {
		// Nothing needs to happen here, we're executing in walk
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "teleport " + entities.toString(e, debug) + " to " + location.toString(e, debug);
	}

}
