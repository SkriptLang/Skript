package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;

public class EvtPortal extends SkriptEvent {

	static {
		Skript.registerEvent("Portal", EvtPortal.class, CollectionUtils.array(PlayerPortalEvent.class, EntityPortalEvent.class), "[player] portal", "entity portal")
				.description(
					"Called when a player or an entity uses a nether or end portal. Note that 'on entity portal' event does not apply to players.",
					"<a href='#EffCancelEvent'>Cancel the event</a> to prevent the entity from teleporting."
				).keywords(
					"player", "entity"
				).examples(
					"on portal:",
						"\tbroadcast \"%player% has entered a portal!\"",
					"",
					"on player portal:",
						"\tplayer's world is world(\"wilderness\")",
						"\tset world of event-location to player's world",
						"\tadd 9000 to x-pos of event-location",
					"",
					"on entity portal:",
						"\tbroadcast \"A %type of event-entity% has entered a portal!"
				).since("1.0, 2.5.3 (entities), 2.13 (location changers)");
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
			return event instanceof PlayerPortalEvent;
		return event instanceof EntityPortalEvent;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isPlayer ? "player" : "entity") + " portal";
	}

}
