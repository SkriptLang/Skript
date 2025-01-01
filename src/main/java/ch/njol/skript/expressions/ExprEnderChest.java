package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@Name("Ender Chest")
@Description("The ender chest of a player.")
@Examples("open the player's ender chest to the player")
@Since("2.0")
public class ExprEnderChest extends SimplePropertyExpression<Player, Inventory> {
	static {
		register(ExprEnderChest.class, Inventory.class, "ender[ ]chest[s]", "players");
	}
	
	@Override
	@Nullable
	public Inventory convert(final Player p) {
		return p.getEnderChest();
	}
	
	@Override
	public Class<? extends Inventory> getReturnType() {
		return Inventory.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "ender chest";
	}
	
}
