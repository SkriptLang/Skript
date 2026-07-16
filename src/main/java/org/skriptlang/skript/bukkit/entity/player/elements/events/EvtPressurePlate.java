package org.skriptlang.skript.bukkit.entity.player.elements.events;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

// TODO: Make this use EntityInteractEvent instead
public class EvtPressurePlate extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPressurePlate.class, "Step on Pressure Plate / Tripwire")
			.supplier(EvtPressurePlate::new)
			.addEvent(PlayerInteractEvent.class)
			.addPatterns(
				"[step[ping] on] [a] [pressure] plate",
				"(trip|[step[ping] on] [a] tripwire)"
			)
			.addDescription("Called when a <i>player</i> steps on a pressure plate or tripwire respectively.")
			.addExample("""
				on step on pressure plate:
					chance of 25%:
						 spawn primed tnt above block at player
						 send "Run!" to player
				""")
			.addSince("1.0 (pressure plate), 1.4.4 (tripwire)")
			.build());

		// event values for this are handled in #EvtClick
	}

	private boolean tripwire;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		tripwire = matchedPattern == 1;
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerInteractEvent playerEvent = (PlayerInteractEvent) event;
		Block clickedBlock = playerEvent.getClickedBlock();
		Material type = clickedBlock == null ? null : clickedBlock.getType();

		if (type == null || playerEvent.getAction() != Action.PHYSICAL)
			return false;

		if (tripwire)
			return type == Material.TRIPWIRE || type == Material.TRIPWIRE_HOOK;

		return Tag.PRESSURE_PLATES.isTagged(type);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return tripwire ? "trip on a tripwire" : "stepping on a pressure plate";
	}

}
