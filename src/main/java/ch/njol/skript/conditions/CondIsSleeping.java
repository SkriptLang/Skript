package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Sleeping")
@Description("Checks whether a player is sleeping.")
@Examples({
	"# cut your enemies' throats in their sleep >=)",
	"on attack:",
		"\tattacker is holding a sword",
		"\tvictim is sleeping",
		"\tincrease the damage by 1000"
})
@Since("1.4.4")
public class CondIsSleeping extends PropertyCondition<Player> {
	
	static {
		register(CondIsSleeping.class, "sleeping", "players");
	}
	
	@Override
	public boolean check(Player player) {
		return player.isSleeping();
	}
	
	@Override
	protected String getPropertyName() {
		return "sleeping";
	}
	
}
