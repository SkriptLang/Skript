package ch.njol.skript.conditions;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

/**
 * @author Peter Güttinger
 */
@Name("Is Blocking")
@Description("Checks whether a player is blocking with their shield.")
@Examples({"on damage of player:",
	  	"	victim is blocking",
	 	"	damage attacker by 0.5 hearts"})
@Since("<i>unknown</i> (before 2.1)")
public class CondIsBlocking extends PropertyCondition<Player> {
	
	static {
		register(CondIsBlocking.class, "(blocking|defending) [with [a] shield]", "players");
	}
	
	@Override
	public boolean check(final Player p) {
		return p.isBlocking();
	}
	
	@Override
	protected String getPropertyName() {
		return "blocking";
	}
	
}
