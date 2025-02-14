package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Sneaking")
@Description("Checks whether a player is sneaking.")
@Examples({
	"# prevent mobs from seeing sneaking players if they are at least 4 meters apart",
	"on target:",
		"\ttarget is sneaking",
		"\tdistance of target and the entity is bigger than 4",
		"\tcancel the event"
})
@Since("1.4.4")
public class CondIsSneaking extends PropertyCondition<Player> {
	
	static {
		register(CondIsSneaking.class, "sneaking", "players");
	}
	
	@Override
	public boolean check(Player player) {
		return player.isSneaking();
	}
	
	@Override
	protected String getPropertyName() {
		return "sneaking";
	}
	
}
