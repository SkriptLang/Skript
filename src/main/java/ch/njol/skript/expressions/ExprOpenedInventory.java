package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.InventoryUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("Opened Inventory")
@Description({"Return the currently opened inventory of a player.",
	"If no inventory is open, it returns the own player's crafting inventory."})
@Examples({"set slot 1 of player's current inventory to diamond sword"})
@Since("2.2-dev24, 2.2-dev35 (Just 'current inventory' works in player events)")
public class ExprOpenedInventory extends PropertyExpression<Player, Inventory> {

	static {
		Skript.registerExpression(ExprOpenedInventory.class, Inventory.class, ExpressionType.PROPERTY, "[the] (current|open|top) inventory [of %players%]", "%players%'[s] (current|open|top) inventory");
	}

	@Override
	public Class<? extends Inventory> getReturnType() {
		return Inventory.class;
	}

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<Player>) exprs[0]);
		return true;
	}

	@Override
	protected Inventory[] get(Event event, Player[] source) {
		return get(source, player -> InventoryUtils.getTopInventory(player.getOpenInventory()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "current inventory" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(event, debug));
	}
	
}
