package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Flying")
@Description("Checks whether a player is flying.")
@Example("player is not flying")
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
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(Player player, boolean flying, ChangeMode mode) {
		player.setFlying(flying);
	}

	@Override
	protected String getPropertyName() {
		return "flying";
	}
	
}
