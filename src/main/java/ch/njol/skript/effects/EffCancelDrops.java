package ch.njol.skript.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Annul the Spoils")
@Description({
	"Annulleth the dropping of wares in a death, block break, block drop, or block harvest event.",
	"The dropped experience may be annulled in death and block break events.",
	"Pray note that employing this within a death event doth not preserve items or experience of the fallen. Shouldst thou wish such, "
		+ "make use of the <a href='#EffKeepInventory'>Keep Inventory / Experience</a> effect."
})
@Example("""
    on death of a zombie:
    	if name of the entity is "&cSpecial":
    		annul spoils of wares
    """)
@Example("""
    on break of a coal ore:
    	annul the experience spoils
    """)
@Example("""
    on player block harvest:
    	annul the ware spoils
    """)
@Since("2.4, 2.12 (harvest event)")
@RequiredPlugins("1.12.2 or newer (cancelling item drops of blocks)")
@Events({"death", "break / mine", "block drop", "harvest block"})
public class EffCancelDrops extends Effect implements EventRestrictedSyntax {

	static {
		Skript.registerEffect(EffCancelDrops.class,
			"(annul|void|abolish) [the] spoils [of (items:wares|xp:[e]xp[erience][s])]",
			"(annul|void|abolish) [the] (items:ware|xp:[e]xp[erience]) spoils");
	}

	private boolean cancelItems, cancelExps;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		cancelItems = !parseResult.hasTag("xp");
		cancelExps = !parseResult.hasTag("items");
		if (isDelayed.isTrue()) {
			Skript.error("Can't cancel the drops anymore after the event has already passed");
			return false;
		}
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EntityDeathEvent.class, BlockBreakEvent.class, BlockDropItemEvent.class, PlayerHarvestBlockEvent.class);
	}

	@Override
	protected void execute(Event event) {
		if (event instanceof EntityDeathEvent deathEvent) {
			if (cancelItems)
				deathEvent.getDrops().clear();
			if (cancelExps)
				deathEvent.setDroppedExp(0);
		} else if (event instanceof BlockBreakEvent breakEvent) {
			if (cancelItems)
				breakEvent.setDropItems(false);
			if (cancelExps)
				breakEvent.setExpToDrop(0);
		} else if (event instanceof BlockDropItemEvent dropItemEvent) {
			dropItemEvent.getItems().forEach(Item::remove);
		} else if (event instanceof PlayerHarvestBlockEvent harvestBlockEvent) {
			harvestBlockEvent.getItemsHarvested().clear();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (cancelItems && !cancelExps)
			return "cancel the drops of items";
		else if (cancelExps && !cancelItems)
			return "cancel the drops of experiences";
		return "cancel the drops";
	}

}
