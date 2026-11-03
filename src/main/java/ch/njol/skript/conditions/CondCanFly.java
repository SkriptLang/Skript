package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Can Fly")
@Description("Whether a player is allowed to fly.")
@Example("player can fly")
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
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET || mode == ChangeMode.RESET;
	}

	@Override
	protected void change(Player player, boolean allowFlight, ChangeMode mode) {
		if (mode == ChangeMode.RESET) {
			allowFlight = switch (player.getGameMode()) {
				case CREATIVE, SPECTATOR -> true;
				default -> false;
			};
		}
		player.setAllowFlight(allowFlight);
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
