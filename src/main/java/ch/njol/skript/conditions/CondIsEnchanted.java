package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Is Enchanted")
@Description("Checks whether an item is enchanted.")
@Examples({"tool of the player is enchanted with efficiency 2",
		"helm, chestplate, leggings or boots are enchanted"})
@Since("1.4.6")
public class CondIsEnchanted extends Condition {
	
	static {
		PropertyCondition.register(CondIsEnchanted.class, "enchanted [with %-enchantmenttype%]", "itemtypes");
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@Nullable
	private Expression<EnchantmentType> enchs;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		enchs = (Expression<EnchantmentType>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		if (enchs != null)
			return items.check(e, item -> enchs.check(e, item::hasEnchantments), isNegated());
		else
			return items.check(e, ItemType::hasEnchantments, isNegated());
		
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final Expression<EnchantmentType> es = enchs;
		
		return PropertyCondition.toString(this, PropertyType.BE, e, debug, items,
				"enchanted" + (es == null ? "" : " with " + es.toString(e, debug)));
	}
	
}
