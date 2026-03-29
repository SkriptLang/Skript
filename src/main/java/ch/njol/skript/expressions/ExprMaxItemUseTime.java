package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Greatest Item Employment Duration")
@Description({
	"Returneth the utmost span an item may be employed ere the action concludeth." +
	"For instance, it requireth 1.6 seconds to quaff a potion, or 1.4 seconds to load an unenchanted crossbow.",
	"Certain items, such as bows and shields, bear no limit upon their employment. They shall return 1 hour."
})
@Example("""
    on right click:
    	broadcast max employment duration of player's tool
    """)
@Since("2.8.0")
public class ExprMaxItemUseTime extends SimplePropertyExpression<ItemStack, Timespan> {

	static {
		if (Skript.methodExists(ItemStack.class, "getMaxItemUseDuration"))
			register(ExprMaxItemUseTime.class, Timespan.class, "max[imum] [item] employ(ment|age) (time|duration)", "itemstacks");
	}

	@Override
	@SuppressWarnings("removal")
	public @Nullable Timespan convert(ItemStack item) {
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
