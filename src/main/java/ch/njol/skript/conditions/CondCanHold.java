package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Can Hold")
@Description("Tests whether a player or a chest can hold the given item.")
@Examples({"block can hold 200 cobblestone",
		"player has enough space for 64 feathers"})
@Since("1.0")
public class CondCanHold extends Condition {
	
	static {
		Skript.registerCondition(CondCanHold.class,
				"%inventories% (can hold|ha(s|ve) [enough] space (for|to hold)) %itemtypes%",
				"%inventories% (can(no|')t hold|(ha(s|ve) not|ha(s|ve)n't|do[es]n't have) [enough] space (for|to hold)) %itemtypes%");
	}
	
	@SuppressWarnings("null")
	private Expression<Inventory> invis;
	@SuppressWarnings("null")
	private Expression<ItemType> items;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		invis = (Expression<Inventory>) exprs[0];
		items = (Expression<ItemType>) exprs[1];
		if (items instanceof Literal) {
			for (ItemType t : ((Literal<ItemType>) items).getAll()) {
				t = t.getItem();
				if (!(t.isAll() || t.getTypes().size() == 1)) {
					Skript.error("The condition 'can hold' can currently only be used with aliases that start with 'every' or 'all', or only represent one item.", ErrorQuality.SEMANTIC_ERROR);
					return false;
				}
			}
		}
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return invis.check(e,
				invi -> {
					if (!items.getAnd()) {
						return items.check(e,
								t -> t.getItem().hasSpace(invi));
					}
					final ItemStack[] buf = ItemType.getStorageContents(invi);
					return items.check(e,
							t -> t.getItem().addTo(buf));
				}, isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.CAN, e, debug, invis,
				"hold " + items.toString(e, debug));
	}
	
}
