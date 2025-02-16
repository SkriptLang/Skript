package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Player;

@Name("Is Sprinting")
@Description("Checks whether a player is sprinting.")
@Examples("player is not sprinting")
@Since("1.4.4")
public class CondIsSprinting extends PropertyCondition<Player> {
	
	static {
		register(CondIsSprinting.class, "sprinting", "players");
	}
	
	@Override
	public boolean check(Player player) {
		return player.isSprinting();
	}
	
	@Override
	protected String getPropertyName() {
		return "sprinting";
	}
	
}
