package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
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
import ch.njol.util.Kleenean;

@Name("Damaged Item")
@Description({
		"Changes the durability of an item.",
		"Damage is used to remove the specified number from the base durability of an item, e.g. 400 - damage. "
		+ "Durability is used to modify the total durability."
})
@Examples({"give player diamond sword with damage value 100", "set player's tool to diamond hoe damaged by 250",
		"give player diamond sword with damage 700 named \"BROKEN SWORD\"",
		"set {_item} to diamond hoe with damage value 50 named \"SAD HOE\"",
		"set target block of player to wool with data value 1", "set target block of player to potato plant with data value 7",
		"give player wooden sword with 1 durability named \"VERY BROKEN SWORD\"",
		"set player's tool to diamond hoe with durability 500"})
@Since("2.4, INSERT VERSION (with durability)")
public class ExprDamagedItem extends PropertyExpression<ItemType, ItemType> {

	static {
		Skript.registerExpression(ExprDamagedItem.class, ItemType.class, ExpressionType.COMBINED,
				"%itemtypes% with (damage|data) [value] %number%",
				"%itemtypes% damaged by %number%",
				"%itemtypes% with durability %number%",
				"%itemtypes% with %number% durability");
	}

	@SuppressWarnings("null")
	private Expression<Number> damage;
	private boolean hasDurability;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		damage = (Expression<Number>) exprs[1];
		hasDurability = matchedPattern > 1 ? true : false;
		if (matchedPattern == 0) {
			Skript.warning("Data value is deprecated and is only used in older Minecraft versions that do not support this Skript version.");
		}
		return true;
	}

	@Override
	protected ItemType[] get(Event event, ItemType[] source) {
		Number damage = this.damage.getSingle(event);
		if (damage == null)
			return source;
		return get(source.clone(), item -> {
			if (hasDurability == true) {
				ItemUtils.setDamage(item, ItemUtils.getMaxDamage(item) - damage.intValue());
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
	public String toString(@Nullable Event event, boolean debug) {
		if (hasDurability == true)
			return getExpr().toString(event, debug) + " with durability " + damage.toString(event, debug);

		return getExpr().toString(event, debug) + " with damage value " + damage.toString(event, debug);
	}

}
