package ch.njol.skript.events;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class EvtPressurePlate extends SkriptEvent {

	static {
		// TODO is EntityInteractEvent similar for entities?
		Skript.registerEvent("Pressure Plate / Trip", EvtPressurePlate.class, PlayerInteractEvent.class,
				"[step[ping] on] [a] [pressure] plate",
				"(trip|[step[ping] on] [a] tripwire)")
				.description("Called when a <i>player</i> steps on a pressure plate or tripwire respectively.")
				.examples("on step on pressure plate:")
				.since("1.0 (pressure plate), 1.4.4 (tripwire)");
	}
	
	private boolean tripwire;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		tripwire = matchedPattern == 1;
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
		Block clickedBlock = interactEvent.getClickedBlock();
		Material type = clickedBlock == null ? null : clickedBlock.getType();
		if (type == null || interactEvent.getAction() != Action.PHYSICAL)
			return false;

		if (tripwire)
			return(type == Material.TRIPWIRE || type == Material.TRIPWIRE_HOOK);

		return Tag.PRESSURE_PLATES.isTagged(type);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return tripwire ? "trip" : "stepping on a pressure plate";
	}
	
}
