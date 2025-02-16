package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Armor Change Item")
@Description("Get the unequipped or equipped armor item from a 'armor change' event.")
@Examples({
	"on armor change:",
		"\tbroadcast the old armor item"
})
@RequiredPlugins("Paper")
@Events("Armor Change")
@Since("INSERT VERSION")
public class ExprArmorChangeItem extends SimpleExpression<ItemStack> implements EventRestrictedSyntax {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerArmorChangeEvent"))
			Skript.registerExpression(ExprArmorChangeItem.class, ItemStack.class, ExpressionType.SIMPLE,
				"[the] (old|unequipped) armo[u]r item",
				"[the] (new|equipped) armo[u]r item");
	}

	private boolean oldArmor;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		oldArmor = matchedPattern == 0;
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PlayerArmorChangeEvent.class);
	}

	@Override
	protected ItemStack @Nullable [] get(Event event) {
		if (!(event instanceof PlayerArmorChangeEvent changeEvent))
			return null;
		return new ItemStack[]{oldArmor ? changeEvent.getOldItem() : changeEvent.getNewItem()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (oldArmor ? "old" : "new") + " armor item";
	}

}
