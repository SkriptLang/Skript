package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Enchanted")
@Description("Checks whether an item is enchanted. Enchants must match the exact level by default, unless 'or better' is used.")
@Example("tool of the player is enchanted with efficiency 2")
@Example("if player's helmet or player's boots are enchanted with protection 3 or better:")
@Since("1.4.6, INSERT VERSION ('or better')")
public class CondIsEnchanted extends Condition {
	
	static {
		PropertyCondition.register(CondIsEnchanted.class, "enchanted [with %-enchantmenttypes% [atleast:or (better|greater)]]", "itemtypes");
	}

	private Expression<ItemType> items;
	private @Nullable Expression<EnchantmentType> enchs;
	private boolean exact;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		enchs = (Expression<EnchantmentType>) exprs[1];
		exact = !parseResult.hasTag("atleast");
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		if (enchs != null) {
			EnchantmentType[] enchantments = enchs.getAll(event);
			boolean and = enchs.getAnd();
			return items.check(event, item ->
				exact ? item.hasExactEnchantments(and, enchantments)
					: item.hasEnchantments(and, enchantments),
				isNegated());
		} else {
			return items.check(event, ItemType::hasEnchantments, isNegated());
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, items,
				"enchanted" + (enchs == null ? "" : " with " + enchs.toString(event, debug)) +
					(exact ? "" : " or better"));
	}
	
}
