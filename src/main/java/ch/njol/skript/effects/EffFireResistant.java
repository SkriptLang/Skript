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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Render Proof Against Flame")
@Description("Render items proof against the ravages of fire.")
@Example("render player's tool proof against flame")
@Example("render {_items::*} not resistant to fire")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.9.0")
public class EffFireResistant extends Effect {

	static {
		if (Skript.methodExists(ItemMeta.class, "setFireResistant", boolean.class)) {
			Skript.registerEffect(EffFireResistant.class, "render %itemtypes% [:not] (proof against flame|resistant to fire)");
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	private boolean not;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		not = parseResult.hasTag("not");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (ItemType item : this.items.getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			meta.setFireResistant(!not);
			item.setItemMeta(meta);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + items.toString(event, debug) + (not ? " not" : "") + " fire resistant";
	}

}
