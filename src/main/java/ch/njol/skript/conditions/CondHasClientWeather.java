package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Has Client Weather")
@Description("Checks whether the given players have a custom client weather")
@Examples({
	"if the player has custom weather:",
		"\tmessage \"Your custom weather is %player's weather%\""
})
@Since("2.3")
public class CondHasClientWeather extends PropertyCondition<Player> {
	
	static {
		register(CondHasClientWeather.class, PropertyType.HAVE, "[a] (client|custom) weather [set]", "players");
	}
	
	@Override
	public boolean check(Player player) {
		return player.getPlayerWeather() != null;
	}
	
	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}
	
	@Override
	protected String getPropertyName() {
		return "custom weather set";
	}
	
}
