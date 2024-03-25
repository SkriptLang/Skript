package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
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

@Name("Has Adventure Restrictions")
@Description("Check if an item has any adventure restrictions.")
@Examples({
	"player's tool has any build restrictions",
	"{_item} doesn't have a break restriction",
	"{_item} is able to break (stone and dirt)",
	"{_item} can not be placed on diamond ore"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class CondHasAdventureRestrictions extends Condition {

	static {
		if (Skript.methodExists(ItemMeta.class, "hasDestroyableKeys")) {
			Skript.registerCondition(CondHasAdventureRestrictions.class,
				"%itemtypes% (has|have) [a|any] (break|place:build) restriction[s]",
				"%itemtypes% (doesn't|does not|do not|don't) have [a|any] (break|place:build) restriction[s]",
				"%itemtypes% (can|is able to) (break|destroy|mine|place:be placed on) %itemtypes%",
				"%itemtypes% (can't|can[ ]not|is unable to) (break|destroy|mine|place:be placed on) %itemtypes%"
			);
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> keysToCheck;
	private boolean place;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.items = (Expression<ItemType>) exprs[0];
		if (matchedPattern == 0 || matchedPattern == 1)
			this.keysToCheck = (Expression<ItemType>) exprs[1];
		this.place = parseResult.hasTag("place");
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		return true;
	}

	@SuppressWarnings("ConstantValue")
	@Override
	public boolean check(Event e) {
		if (keysToCheck == null)
			return items.check(e, item -> place ? item.getItemMeta().hasPlaceableKeys() : item.getItemMeta().hasDestroyableKeys(), isNegated());
		Set<Namespaced> keys = new HashSet<>();
		for (ItemType item : keysToCheck.getArray(e)) {
			for (ItemStack stack : item.getAll()) {
				keys.add(stack.getType().getKey());
			}
		}

		for (ItemType item : items.getArray(e)) {
			ItemStack stack = item.getRandom();
			if (stack.hasItemMeta()) {
				ItemMeta meta = stack.getItemMeta();
				if (place ? meta.hasPlaceableKeys() : meta.hasDestroyableKeys()) {
					Set<Namespaced> existingKeys = place ? meta.getPlaceableKeys() : meta.getDestroyableKeys();
					for (Namespaced key : keys) {
						if (!existingKeys.contains(key))
							return false;
					}
				}
			}
		}
		return true;
    }

	@SuppressWarnings("ConstantValue")
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (keysToCheck == null)
			return items.toString(event, debug) + " has" + (isNegated() ? " no" : "") + (place ? " placeable" : " destroyable") + " keys";
		return "the" + (place ? " placeable" : " destroyable") + " keys of" + items.toString(event, debug) + (isNegated() ? " do not" : "") + " contain" + keysToCheck.toString(event, debug);
	}
}
