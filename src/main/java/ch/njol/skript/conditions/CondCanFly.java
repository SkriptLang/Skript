package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Can Fly")
@Description("Whether a player is allowed to fly.")
@Examples("player can fly")
@Since("2.3")
public class CondCanFly extends PropertyCondition<Player> {
	
	static {
		register(CondCanFly.class, PropertyType.CAN, "fly", "players");
	}
	
	@Override
	public boolean check(Player player) {
		return player.getAllowFlight();
	}
	
	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}
	
	@Override
	protected String getPropertyName() {
		return "fly";
	}

}
