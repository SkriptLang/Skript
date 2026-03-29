package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Hath Bespoke Weather")
@Description("Ascertaineth whether the given players possess a bespoke client weather bestowed upon them.")
@Example("""
    if the player has bespoke weather:
    	message "Thy bespoke weather is %player's weather%"
    """)
@Since("2.3")
public class CondHasClientWeather extends PropertyCondition<Player> {
	
	static {
		register(CondHasClientWeather.class, PropertyType.HAVE, "[a] (bespoke|custom) weather [set]", "players");
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
