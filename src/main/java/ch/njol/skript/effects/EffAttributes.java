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
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Item Attributes")
@Description({
	"Show or hide the attributes of an item."
})
@Examples({
	"hide the attributes of player's tool",
	"hide {_item}'s attributes"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("INSERT VERSION")
public class EffAttributes extends Effect {
	static {
		Skript.registerEffect(EffAttributes.class,
			"(show|reveal|:hide) %itemtypes%'[s] attributes",
			"(show|reveal|:hide) [the] attributes of %itemtypes%"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	private boolean hide, entire;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		hide = parseResult.hasTag("hide");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (ItemType item : items.getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			if (hide) {
				meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			} else {
				meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			}
			item.setItemMeta(meta);
		}
	}
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (hide ? "hide" : "show") + " the " + "attributes of " + items.toString(event, debug);
	}
}
