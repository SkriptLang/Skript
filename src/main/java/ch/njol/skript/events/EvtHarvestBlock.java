package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EvtHarvestBlock extends SkriptEvent {

	static {
		Skript.registerEvent("Harvest Block", EvtHarvestBlock.class, PlayerHarvestBlockEvent.class,
			"[player] [block|crop] harvest[ing] [of %-itemtypes/blockdatas%]")
			.description("""
				Called when a player harvests a block.
				A block being harvested is when a block drops items and the state of the block is changed, but the block is not broken.
				An example is harvesting berries from a berry bush.
				""")
			.examples("""
				on player block harvest:
					send "You have harvested %event-block% that dropped %event-items% using your %item of event-slot% in your %event-equipment slot%"
				
				on crop harvesting of sweet berry bush:
					chance 5%:
						set drops to a diamond
					chance 1%
						cancel the drops
				""")
			.since("2.12");

		EventValues.registerEventValue(PlayerHarvestBlockEvent.class, Block.class,
			PlayerHarvestBlockEvent::getHarvestedBlock);
		EventValues.registerEventValue(PlayerHarvestBlockEvent.class, ItemStack[].class,
			event -> event.getItemsHarvested().toArray(ItemStack[]::new));
		EventValues.registerEventValue(PlayerHarvestBlockEvent.class, EquipmentSlot.class,
			PlayerHarvestBlockEvent::getHand);
		EventValues.registerEventValue(PlayerHarvestBlockEvent.class, Slot.class,
			event -> new ch.njol.skript.util.slot.EquipmentSlot(event.getPlayer(), event.getHand()));
	}

	private Literal<?> types = null;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		types = args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerHarvestBlockEvent harvestBlockEvent))
			return false;
		if (types == null)
			return true;

		Block block = harvestBlockEvent.getHarvestedBlock();
		BlockData sourceData = block.getBlockData();
		return SimpleExpression.check(types.getAll(), object -> {
			if (object instanceof ItemType itemType) {
				return itemType.isOfType(block);
			} else if (object instanceof BlockData blockData) {
				return blockData.matches(sourceData);
			}
			return false;
		}, false, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("player block harvest");
		if (types != null)
			builder.append("of", types);
		return builder.toString();
	}

}
