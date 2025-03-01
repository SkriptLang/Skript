package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Flying")
@Description("Checks whether a player is flying.")
@Examples("player is not flying")
@Since("1.4.4")
public class CondIsFlying extends PropertyCondition<Player> {
	
	static {
		register(CondIsFlying.class, "flying", "players");
	}
	
	@Override
	public boolean check(Player player) {
		return player.isFlying();
	}
	
	@Override
	protected String getPropertyName() {
		return "flying";
	}
	
}
