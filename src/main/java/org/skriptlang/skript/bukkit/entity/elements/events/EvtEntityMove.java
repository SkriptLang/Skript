package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityMove extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityMove.class, "Entity Move / Rotate")
			.supplier(EvtEntityMove::new)
			.addEvents(CollectionUtils.array(EntityMoveEvent.class, PlayerMoveEvent.class))
			.addPatterns(
				"%entitydata% (move|walk|step|rotate:(turn[ing] around|rotate))",
				"%entitydata% (move|walk|step) or (turn[ing] around|rotate)",
				"%entitydata% (turn[ing] around|rotate) or (move|walk|step)"
			)
			.addDescription("""
                Called when a player or entity moves or rotates their head.
                The move event will only be called when the entity/player moves position,
                keyword 'turn around' is for orientation (ie: looking around),
                and the combined syntax listens for both.
                Note that this event is called extremely often and may cause performance issues.
                """)
			.addExample("""
                on player move:
                    if {frozen::%player's uuid%} is set:
                        cancel event
                        send actionbar "You are frozen!" to player
                        // Generally not recommended over setting movement & jump attributes but useful for older servers
                """)
			.addExample("""
                on player turning around:
                    send "BOO!" to player
                    spawn skeleton behind player
                """)
			.addExample("""
				on skeleton move:
				    if event-world is "no_skeleton_movement":
				        cancel event
				""")
			.addSince("2.6, 2.8.0 (turn around)")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityMoveEvent.class, Location.class)
			.getter(EntityMoveEvent::getFrom)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityMoveEvent.class, Location.class)
			.getter(EntityMoveEvent::getTo)
			.time(Time.FUTURE)
			.build());
	}

	private EntityData<?> entityData;
	private boolean isPlayer;
	private boolean canBePlayer;
	private Move moveType;

	private enum Move {
		MOVE("move"),
		MOVE_OR_ROTATE("move or rotate"),
		ROTATE("rotate");

		private final String name;

		Move(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		entityData = ((Literal<EntityData<?>>) args[0]).getSingle();
		isPlayer = Player.class.isAssignableFrom(entityData.getType());
		canBePlayer = entityData.getType().isAssignableFrom(Player.class);
		if (matchedPattern > 0) {
			moveType = Move.MOVE_OR_ROTATE;
		} else if (parseResult.hasTag("rotate")) {
			moveType = Move.ROTATE;
		} else {
			moveType = Move.MOVE;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		Location from, to;
		if (canBePlayer && event instanceof PlayerMoveEvent playerEvent) {
			from = playerEvent.getFrom();
			to = playerEvent.getTo();
		} else if (event instanceof EntityMoveEvent entityEvent) {
			if (!(entityData.isInstance(entityEvent.getEntity())))
				return false;
			from = entityEvent.getFrom();
			to = entityEvent.getTo();
		} else {
			return false;
		}
		return switch (moveType) {
			case MOVE -> hasChangedPosition(from, to);
			case ROTATE -> hasChangedOrientation(from, to);
			case MOVE_OR_ROTATE -> true;
		};
	}

	/**
	 * Checks if the from location is different from the to location.
	 * @param from The location from.
	 * @param to The location to.
	 * @return Whether from matches to.
	 */
	private static boolean hasChangedPosition(Location from, Location to) {
		return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ() || from.getWorld() != to.getWorld();
	}


	/**
	 * Checks if the yaw and pitch of from is different from the yaw and pitch of to.
	 * @param from The location from.
	 * @param to The location to.
	 * @return Whether the orientation has changed or not.
	 */
	private static boolean hasChangedOrientation(Location from, Location to) {
		return from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Event>[] getEventClasses() {
		if (isPlayer) {
			return new Class[]{PlayerMoveEvent.class};
		}
		if (canBePlayer)
			return new Class[]{EntityMoveEvent.class, PlayerMoveEvent.class};
		return new Class[]{EntityMoveEvent.class};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return entityData + " " + moveType;
	}

}
