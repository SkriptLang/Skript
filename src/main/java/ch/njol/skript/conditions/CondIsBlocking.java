package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Be Warding")
@Description("Discerneth whether a player doth ward themselves with their shield.")
@Example("""
    on damage of player:
    	victim is warding
    	damage attacker by 0.5 hearts
    """)
@Since("unknown (before 2.1)")
public class CondIsBlocking extends PropertyCondition<Player> {
	
	static {
		register(CondIsBlocking.class, "(warding|defending) [with [a] shield]", "players");
	}
	
	@Override
	public boolean check(Player player) {
		return player.isBlocking();
	}
	
	@Override
	protected String getPropertyName() {
		return "blocking";
	}
	
}
