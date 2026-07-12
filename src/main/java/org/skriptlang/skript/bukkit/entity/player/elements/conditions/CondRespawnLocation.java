package org.skriptlang.skript.bukkit.entity.player.elements.conditions;

import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.player.AbstractRespawnEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Bed/Anchor Spawn")
@Description("Checks what the respawn location of a player in the respawn event is.")
@Example("""
	on respawn:
		the respawn location is a bed
		broadcast "%player% is respawning in their bed! So cozy!"
	""")
@RequiredPlugins("Minecraft 1.16+")
@Since("2.7")
@Events("respawn")
public class CondRespawnLocation extends Condition implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondRespawnLocation.class)
			.supplier(CondRespawnLocation::new)
			.addPattern("[the] respawn location (was|is)[1:(n'| no)t] [a] (:bed|respawn anchor)")
			.build());
	}

	private boolean bedSpawn;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.mark == 1);
		bedSpawn = parseResult.hasTag("bed");
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(AbstractRespawnEvent.class);
	}

	@Override
	public boolean check(Event event) {
		if (event instanceof AbstractRespawnEvent respawnEvent) {
			return (bedSpawn ? respawnEvent.isBedSpawn() : respawnEvent.isAnchorSpawn()) != isNegated();
		}

		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the respawn location " + (isNegated() ? "isn't" : "is") + " a " + (bedSpawn ? "bed spawn" : "respawn anchor");
	}

}
