package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.Namespaced;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Name("Apply Adventure Restrictions")
@Description("Allow or prevent an item to destroy or be placed on certain types of blocks while in /gamemode adventure.")
@Examples({
	"allow player's tool to destroy (stone, oak wood planks)",
	"prevent {_item} from being placed on (diamond ore, diamond block)"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class EffAdventureRestrictions extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "setDestroyableKeys")) {
			Skript.registerEffect(EffAdventureRestrictions.class,
				"allow %~itemtypes% to (destroy|break|mine|place:be placed on) %itemtypes%",
				"(disallow|prevent) %~itemtypes% from (destroying|breaking|mining|place:being placed on) %itemtypes%");
			// should this require notnull? and can the patterns here be made any better?
		}
	}

	private boolean allow;
	private boolean destroy;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> deltaKeys;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.items = (Expression<ItemType>) exprs[0];
		this.deltaKeys = (Expression<ItemType>) exprs[1];
		this.allow = matchedPattern == 0;
		this.destroy = !parseResult.hasTag("place");
		return true;
	}

	@Override
	protected void execute(Event event) {
		ItemType[] items = this.items.getArray(event);
		if (items.length == 0)
			return;

		Set<Namespaced> keys = new HashSet<>();

		for (ItemType itemType : deltaKeys.getArray(event)) {
			for (ItemStack item : itemType.getAll()) {
				keys.add(item.getType().getKey());
			}
		}

		if (!keys.isEmpty()) {
			for (ItemType item : items) {

				if (item.getRandom().hasItemMeta()) {
					ItemMeta meta = item.getItemMeta();
                    Set<Namespaced> existingKeys = new HashSet<>(destroy ? meta.getDestroyableKeys() : meta.getPlaceableKeys());
					if (allow) {
						existingKeys.addAll(keys);
					} else {
						existingKeys.removeAll(keys);
					}
					if (destroy) { meta.setDestroyableKeys(existingKeys); } else { meta.setPlaceableKeys(existingKeys); }
					if (destroy ? meta.hasDestroyableKeys() : meta.hasPlaceableKeys())
						item.setItemMeta(meta);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (allow)
			return "allow " + items.toString(event, debug) + " to " + (destroy ? "destroy " : "be placed on ") + deltaKeys.toString(event, debug);
		return "prevent " + items.toString(event, debug) + " from " + (destroy ? "destroying " : "being placed on ") + deltaKeys.toString(event, debug);
	}
}
