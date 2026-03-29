package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Item Tooltips")
@Description({
	"Reveal or conceal the tooltip of an item.",
	"If altering the 'entire' tooltip of an item, naught shall appear when a player hovers upon it.",
	"If altering the 'supplementary' tooltip, only particular portions (which vary per item) shall be concealed."
})
@Example("conceal the entire tooltip of player's tool")
@Example("conceal {_item}'s supplementary tool tip")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public class EffTooltip extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "setHideTooltip", boolean.class)) { // this method was added in the same version as the additional tooltip item flag
			Skript.registerEffect(EffTooltip.class,
				"(reveal|unveil|hide:conceal) %itemtypes%'[s] [entire|additional:supplementary] tool[ ]tip",
				"(reveal|unveil|hide:conceal) [the] [entire|additional:supplementary] tool[ ]tip of %itemtypes%"
			);
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	private boolean hide, entire;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		hide = parseResult.hasTag("hide");
		entire = !parseResult.hasTag("additional");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (ItemType item : items.getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			if (entire) {
				meta.setHideTooltip(hide);
			} else {
				if (hide) {
					meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
				} else {
					meta.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
				}
			}
			item.setItemMeta(meta);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (hide ? "hide" : "show") + " the " + (entire ? "entire" : "additional") + " tooltip of " + items.toString(event, debug);
	}

}
