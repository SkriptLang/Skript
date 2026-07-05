package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityPortal extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityPortal.class, "Entity/Player Portal")
			.supplier(EvtEntityPortal::new)
			.addEvents(CollectionUtils.array(PlayerPortalEvent.class, EntityPortalEvent.class))
			.addPatterns("[player] portal", "entity portal")
			.addDescription("""
				Called when a player or an entity uses a nether or end portal.
				Note that 'on entity portal' event does not apply to players.
				Use <a href='#EffCancelEvent'>Cancel the event</a> to prevent the entity from teleporting.
				""")
			.addExample("""
				on portal:
					broadcast "An entity has entered a portal!"
				""")
			.addExample("""
				on player portal:
					player's world is world("wilderness")
					set world of event-location to player's world
					add 9000 to x-pos of event-location
				""")
			.addSince("1.0, 2.5.3 (entities), 2.13 (location changers)")
			.build());
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
		return isPlayer ? "player" : "entity" + "portal";
	}

}
