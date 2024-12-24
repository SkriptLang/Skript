package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.CursorSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;

/**
 * Cursor item slot is not actually an inventory slot, but an item which the player
 * has in their cursor when any inventory is open for them.
 */
@Name("Cursor Slot")
@Description("The item which the player has on their inventory cursor. This slot is always empty if player has no inventory open.")
@Examples({
	"cursor slot of player is dirt",
	"set cursor slot of player to 64 diamonds"
})
@Since("2.2-dev17")
public class ExprCursorSlot extends PropertyExpression<Player, Slot> {

	static {
		register(ExprCursorSlot.class, Slot.class, "cursor slot", "players");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Player>) exprs[0]);
		return true;
	}

	@Override
	protected Slot[] get(Event event, Player[] source) {
		return get(source, player -> {
			if (event instanceof InventoryClickEvent)
				return new CursorSlot(player, ((InventoryClickEvent) event).getCursor());
			return new CursorSlot(player);
		});
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cursor slot of " + getExpr().toString(event, debug);
	}

}
