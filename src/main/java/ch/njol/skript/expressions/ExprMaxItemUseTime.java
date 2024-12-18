package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Max Item Use Time")
@Description({
	"Returns the max duration an item can be used for before the action completes. " +
	"E.g. it takes 1.6 seconds to drink a potion, or 1.4 seconds to load an unenchanted crossbow.",
	"Some items, like bows and shields, do not have a limit to their use. They will return 1 hour."
})
@Examples({
	"on right click:",
		"\tbroadcast max usage duration of player's tool"
})
@Since("2.8.0")
@RequiredPlugins("Paper")
public class ExprMaxItemUseTime extends SimplePropertyExpression<ItemStack, Timespan> {

	static {
		if (Skript.methodExists(ItemStack.class, "getMaxItemUseDuration"))
			register(ExprMaxItemUseTime.class, Timespan.class, "max[imum] [item] us(e|age) (time|duration)", "itemstacks");
	}

	@Override
	@Nullable 
	public Timespan convert(ItemStack item) {
		return new Timespan(Timespan.TimePeriod.TICK, item.getMaxItemUseDuration());
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "maximum usage time";
	}

}
