package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.SkriptTeleportFlag;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.EffSecSpawn.SpawnEvent;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.Direction;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import io.papermc.lib.PaperLib;
import io.papermc.lib.environments.PaperEnvironment;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@Name("Teleport")
@Description({
	"Teleport an entity to a specific location. ",
	"This effect is delayed by default on Paper, meaning certain syntax such as the return effect for functions cannot be used after this effect.",
	"The keyword 'force' indicates this effect will not be delayed, ",
	"which may cause lag spikes or server crashes when using this effect to teleport entities to unloaded chunks.",
	"Teleport flags are settings to retain during a teleport. Such as direction, passengers, x coordinate, etc."
})
@Examples({
	"teleport the player to {home::%uuid of player%}",
	"teleport the attacker to the victim",
	"",
	"on dismount:",
		"\tcancel event",
		"\tteleport the player to {server::spawn} retaining vehicle and passengers"
})
@RequiredPlugins("Paper 1.19+ (teleport flags)")
@Since("1.0, 2.10 (flags)")
public class EffTeleport extends Effect {

	private static final boolean TELEPORT_FLAGS_SUPPORTED = Skript.classExists("io.papermc.paper.entity.TeleportFlag");
	private static final boolean CAN_RUN_ASYNC = PaperLib.getEnvironment() instanceof PaperEnvironment;

	static {
		String extra = "";
		if (TELEPORT_FLAGS_SUPPORTED)
			extra = " [[while] retaining %-teleportflags%]";
		Skript.registerEffect(EffTeleport.class, "[:force] teleport %entities% (to|%direction%) %location%" + extra);
	}

	private @Nullable Expression<SkriptTeleportFlag> teleportFlags;
	private Expression<Entity> entities;
	private Expression<Location> location;
	private boolean async;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		location = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		async = CAN_RUN_ASYNC && !parseResult.hasTag("force");
		if (TELEPORT_FLAGS_SUPPORTED)
			teleportFlags = (Expression<SkriptTeleportFlag>) exprs[3];

		if (getParser().isCurrentEvent(SpawnEvent.class)) {
			Skript.error("You cannot teleport an entity that hasn't spawned yet. Ensure you're using the location expression from the spawn section pattern.");
			return false;
		}

		if (async)
			getParser().setHasDelayBefore(Kleenean.UNKNOWN); // UNKNOWN because it isn't async if the chunk is already loaded.
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		debug(event, true);
		TriggerItem next = getNext();

		boolean delayed = Delay.isDelayed(event);
		Location location = this.location.getSingle(event);
		if (location == null)
			return next;
		boolean unknownWorld = !location.isWorldLoaded();

		Entity[] entityArray = entities.getArray(event); // We have to fetch this before possible async execution to avoid async local variable access.
		if (entityArray.length == 0)
			return next;

		if (!delayed) {
			if (event instanceof PlayerRespawnEvent playerRespawnEvent && entityArray.length == 1 && entityArray[0].equals(playerRespawnEvent.getPlayer())) {
				if (unknownWorld)
					return next;
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

		if (!async) {
			SkriptTeleportFlag[] teleportFlags = this.teleportFlags == null ? null : this.teleportFlags.getArray(event);
			for (Entity entity : entityArray) {
				teleport(entity, location, teleportFlags);
			}
			return next;
		}

		final Location fixed = location;
		Delay.addDelayedEvent(event);
		Object localVars = Variables.removeLocals(event);

		// This will either fetch the chunk instantly if on Spigot or already loaded or fetch it async if on Paper.
		PaperLib.getChunkAtAsync(location).thenAccept(chunk -> {
			// The following is now on the main thread
			SkriptTeleportFlag[] teleportFlags = this.teleportFlags == null ? null : this.teleportFlags.getArray(event);
			for (Entity entity : entityArray) {
				teleport(entity, fixed, teleportFlags);
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
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
				.append("teleport", entities, "to", location);
		if (teleportFlags != null)
			builder.append("retaining", teleportFlags);
		return builder.toString();
	}

	private void teleport(@NotNull Entity entity, @NotNull Location location, SkriptTeleportFlag... skriptTeleportFlags) {
		if (location.getWorld() == null) {
			location = location.clone();
			location.setWorld(entity.getWorld());
		}

		if (!TELEPORT_FLAGS_SUPPORTED || skriptTeleportFlags == null) {
			entity.teleport(location);
			return;
		}

		Stream<TeleportFlag> teleportFlags = Arrays.stream(skriptTeleportFlags)
				.flatMap(teleportFlag -> Stream.of(teleportFlag.getTeleportFlags()))
				.filter(Objects::nonNull);
		entity.teleport(location, teleportFlags.toArray(TeleportFlag[]::new));
	}

}
