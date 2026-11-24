package ch.njol.skript.expressions;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.util.Kleenean;

@Name("Damaged Item")
@Description("Directly damages an item.")
@Example("give player diamond sword with damage value 100")
@Example("set player's tool to diamond hoe damaged by 250")
@Example("give player diamond sword with damage 700 named \"BROKEN SWORD\"")
@Example("set {_item} to diamond hoe with damage value 50 named \"SAD HOE\"")
@Since("2.4")
public class ExprDamagedItem extends PropertyExpression<ItemType, ItemType> {
	
	static {
		Skript.registerExpression(ExprDamagedItem.class, ItemType.class, ExpressionType.COMBINED,
				"%itemtype% with (damage|data) [value] %number%",
				"%itemtype% damaged by %number%");
	}
	
	@SuppressWarnings("null")
	private Expression<Number> damage;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) expressions[0]);
		damage = (Expression<Number>) expressions[1];
		return true;
	}
	
	@Override
	protected ItemType[] get(Event event, ItemType[] source) {
		Number damage = this.damage.getSingle(event);
		if (damage == null)
			return source;

		return get(source, item -> {
			ItemType newItem = item.clone();
			newItem.iterator().forEachRemaining(i -> i.setDurability(damage.intValue()));
			return newItem;
		});
	}
	
	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + " with damage value " + damage.toString(event, debug);
	}
	
}
