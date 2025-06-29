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
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Name("Has Item Attributes")
@Description({
	"Whether the attributes of an item are shown or hidden."
})
@Examples({
	"send true if attributes of player's tool are shown",
	"if attributes of {_item} are hidden:"
})
@RequiredPlugins("Spigot 1.20.5+")
@Since("INSERT VERSION")
public class CondAttributes extends Condition {
	static {
		Skript.registerCondition(CondAttributes.class,
			"[the] attributes of %itemtypes% are (:shown|hidden)",
			"[the] attributes of %itemtypes% (aren't|are not) (:shown|hidden)",
			"%itemtypes%'[s] attributes are (:shown|hidden)",
			"%itemtypes%'[s] attributes (aren't|are not) (:shown|hidden)"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	private boolean entire;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		entire = !parseResult.hasTag("additional");
		setNegated(parseResult.hasTag("shown") ^ (matchedPattern == 1 || matchedPattern == 3));
		return true;
	}

	@Override
	public boolean check(Event event) {
		return items.check(event, item -> item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ATTRIBUTES), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + "attributes of " + items.toString(event, debug) + " are " + (isNegated() ? "hidden" : "shown");
	}
}
