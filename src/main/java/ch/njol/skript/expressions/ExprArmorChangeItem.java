package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Armor Change Item")
@Description("Get the unequipped or equipped armor item from a 'armor change' event.")
@Example("""
	on armor change
		broadcast the old armor item
	""")
@Events("Armor Change")
@Since("2.11")
public class ExprArmorChangeItem extends EventValueExpression<ItemStack> implements EventRestrictedSyntax {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerArmorChangeEvent"))
			register(ExprArmorChangeItem.class, ItemStack.class,
				"(old|unequipped) armo[u]r item",
				"(new|equipped) armo[u]r item");
	}

	public ExprArmorChangeItem() {
		super(ItemStack.class);
	}

	private boolean oldArmor;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		oldArmor = matchedPattern == 0;
		super.setTime(oldArmor ? EventValues.TIME_PAST : EventValues.TIME_FUTURE);
		return super.init(exprs, matchedPattern, isDelayed, parser);
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PlayerArmorChangeEvent.class);
	}

	@Override
	public boolean setTime(int time) {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return oldArmor ? "old armor item" : "new armor item";
	}

}
