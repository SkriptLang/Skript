package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.bukkitutil.ItemUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Damaged Item")
@Description({
	"Directly changes the durability of an item.",
	"Damage is used to remove the specified number from the base durability of an item. E.g 400 - damage",
	"Durability is used to modify the total durability"
})
@Examples({"give player diamond sword with damage value 100", "set player's tool to diamond hoe damaged by 250",
	"give player diamond sword with damage 700 named \"BROKEN SWORD\"",
	"set {_item} to diamond hoe with damage value 50 named \"SAD HOE\"",
	"set target block of player to wool with data value 1", "set target block of player to potato plant with data value 7",
	"give player wooden sword with 1 durability named \"VERY BROKEN SWORD\"",
	"set player's tool to diamond hoe with durability 500"})
@Since("2.4")
public class ExprDamagedItem extends PropertyExpression<ItemType, ItemType> {
	
	static {
		Skript.registerExpression(ExprDamagedItem.class, ItemType.class, ExpressionType.COMBINED,
				"%itemtype% with (damage|data) [value] %number%",
				"%itemtype% damaged by %number%",
				"%itemtype% with durability %number%",
				"%itemtype% with %number% durability");
	}
	
	@SuppressWarnings("null")
	private Expression<Number> damage;
	private int pattern;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		damage = (Expression<Number>) exprs[1];
		pattern = matchedPattern;
		return true;
	}
	
	@Override
	protected ItemType[] get(Event e, ItemType[] source) {
		Number damage = this.damage.getSingle(e);
		if (damage == null)
			return source;
		return get(source.clone(), item -> {
			if (pattern > 1) {
				ItemUtils.setDamage(item, (ItemUtils.getMaxDamage(item) - damage.intValue()));
			} else {
				item.iterator().forEachRemaining(i -> i.setDurability(damage.intValue()));
			}
			return item;
		});
	}
	
	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, boolean debug) {
		if (pattern > 1){
			return getExpr().toString(e, debug) + " with durability " + damage.toString(e, debug);
		}else return getExpr().toString(e, debug) + " with damage value " + damage.toString(e, debug);
	}
}
