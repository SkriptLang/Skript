package ch.njol.skript.events;

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import org.bukkit.PortalType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.coll.CollectionUtils;

public class EvtPortal extends SkriptEvent {

	static {
		Class<? extends Event>[] events = CollectionUtils.array(
			PlayerPortalEvent.class, EntityPortalEvent.class, PlayerTeleportEndGatewayEvent.class,
			EntityTeleportEndGatewayEvent.class
		);
		Skript.registerEvent("Portal / End Gateway", EvtPortal.class, events, "[player] portal", "entity portal")
				.description(
					"Called when a player or an entity uses a nether portal, end portal or end gateway. Note that events with keyword 'entity' does not apply to players.",
					"<a href='#EffCancelEvent'>Cancel the event</a> to prevent the entity from teleporting."
				).keywords(
					"player", "entity"
				).examples(
					"on portal:",
						"\tbroadcast \"%player% has entered a %event-portaltype%!\"",
					"",
					"on player portal:",
						"\tplayer's world is world(\"wilderness\")",
						"\tset world of event-location to player's world",
						"\tadd 9000 to x-pos of event-location",
					"",
					"on entity portal:",
						"\tbroadcast \"A %type of event-entity% has entered a portal!"
				).since("1.0, 2.5.3 (entities), INSERT VERSION (location changers, end gateway)");

		EventValues.registerEventValue(EntityPortalEvent.class, PortalType.class, EntityPortalEvent::getPortalType);
		EventValues.registerEventValue(PlayerPortalEvent.class, PortalType.class, event -> switch (event.getCause()) {
			case END_GATEWAY -> PortalType.END_GATEWAY;
			case END_PORTAL -> PortalType.ENDER;
			case NETHER_PORTAL -> PortalType.NETHER;
			default -> throw new UnsupportedOperationException();
		});
		EventValues.registerEventValue(EntityTeleportEndGatewayEvent.class, PortalType.class, event -> PortalType.END_GATEWAY);
		EventValues.registerEventValue(PlayerTeleportEndGatewayEvent.class, PortalType.class, event -> PortalType.END_GATEWAY);
	}

	private boolean isPlayer;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		isPlayer = matchedPattern == 0;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (isPlayer)
			return event instanceof PlayerPortalEvent || event instanceof PlayerTeleportEndGatewayEvent;
		return event instanceof EntityPortalEvent || event instanceof EntityTeleportEndGatewayEvent;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isPlayer ? "player" : "entity") + " portal";
	}

}
