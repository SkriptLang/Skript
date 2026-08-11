package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.Skript;
import org.skriptlang.skript.bukkit.entity.types.TeleportFlagClassInfo.SkriptTeleportFlag;
import ch.njol.skript.doc.*;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.EffSecSpawn.SpawnEvent;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.Direction;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.stream.Stream;

@Name("Teleport")
@Description("""
	Teleport an entity to a specific location.
	This effect is delayed by default, as it waits for the chunk of the location to be loaded before teleporting.
	The keyword 'force' may be used to bypass this behavior (preventing the delay), but note that it may cause lag spikes or other server performance issues when teleporting to unloaded chunks.
	Teleport flags are properties to retain during a teleport, such as direction, passengers, and velocities.
	""")
@Example("teleport the player to {home::%uuid of player%}")
@Example("teleport the attacker to the victim")
@Example("""
	on dismount:
		cancel event
		teleport the player to {server::spawn} retaining vehicle and passengers
	""")
@Since("1.0, 2.10 (flags)")
public class EffTeleport extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffTeleport.class)
			.supplier(EffTeleport::new)
			.addPattern("[:force] teleport %entities% (to|%direction%) %location% [[while] retaining %-teleportflags%]")
			.build());
	}

	private boolean async;
	private Expression<Entity> entities;
	private Expression<Location> location;
	private @Nullable Expression<SkriptTeleportFlag> teleportFlags;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(SpawnEvent.class)) {
			Skript.error("You cannot teleport an entity that hasn't spawned yet. Ensure you're using the location expression from the spawn section pattern.");
			return false;
		}

		async = !parseResult.hasTag("force");
		entities = (Expression<Entity>) exprs[0];
		location = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		teleportFlags = (Expression<SkriptTeleportFlag>) exprs[3];

		if (async) {
			// UNKNOWN because it isn't async if the chunk is already loaded
			getParser().setHasDelayBefore(Kleenean.UNKNOWN);
		}

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		debug(event, true);
		TriggerItem next = getNext();

		Location location = this.location.getSingle(event);
		if (location == null)
			return next;
		boolean unknownWorld = !location.isWorldLoaded();

		Entity[] entityArray = entities.getArray(event);
		if (entityArray.length == 0)
			return next;

		if (!Delay.isDelayed(event)) { // specific behavior for integration with certain events
			if (event instanceof PlayerRespawnEvent playerRespawnEvent && entityArray.length == 1 && entityArray[0].equals(playerRespawnEvent.getPlayer())) {
				if (!unknownWorld)
					playerRespawnEvent.setRespawnLocation(location);
				return next;
			}

			if (event instanceof PlayerMoveEvent playerMoveEvent && entityArray.length == 1 && entityArray[0].equals(playerMoveEvent.getPlayer())) {
				if (unknownWorld) { // we can approximate the world
					location = location.clone();
					location.setWorld(playerMoveEvent.getFrom().getWorld());
				}
				playerMoveEvent.setTo(location);
				return next;
			}
		}

		if (unknownWorld) { // we can't fetch the chunk without a world
			if (entityArray.length == 1) { // if there's 1 thing we can borrow its world
				Entity entity = entityArray[0];
				if (entity == null)
					return next;
				// assume it's a local teleport, use the first entity we find as a reference
				location = location.clone();
				location.setWorld(entity.getWorld());
			} else {
				return next; // no entities = no chunk = nobody teleporting
			}
		}

		final TeleportFlag[] teleportFlags;
		if (this.teleportFlags == null) {
			teleportFlags = new TeleportFlag[0];
		} else {
			teleportFlags = this.teleportFlags.stream(event)
				.flatMap(teleportFlag -> Stream.of(teleportFlag.getTeleportFlags()))
				.toArray(TeleportFlag[]::new);
		}

		if (!async) {
			for (Entity entity : entityArray) {
				entity.teleport(location, teleportFlags);
			}
			return next;
		}

		final Location fixed = location;
		Object localVars = Variables.removeLocals(event);
		fixed.getWorld().getChunkAtAsync(fixed).thenAccept(ignored -> {
			Delay.addDelayedEvent(event);
			for (Entity entity : entityArray) {
				entity.teleport(fixed, teleportFlags);
			}

			// Re-set local variables
			if (localVars != null)
				Variables.setLocalVariables(event, localVars);
			
			// Continue the rest of the trigger if there is one
			Object timing = null;
			if (next != null) {
				if (SkriptTimings.enabled()) {
					Trigger trigger = getTrigger();
					if (trigger != null) {
						timing = SkriptTimings.start(trigger.getDebugLabel());
					}
				}

				TriggerItem.walk(next, event);
			}
			Variables.removeLocals(event); // Clean up local vars, we may be exiting now
			SkriptTimings.stop(timing);
		});

		return null;
	}

	@Override
	protected void execute(Event event) {
		assert false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("teleport", entities, "to", location)
			.appendIf(teleportFlags != null, "retaining", teleportFlags)
			.toString();
	}

}
