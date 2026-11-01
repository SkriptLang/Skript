package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.OfflinePlayer;

@Name("Is Operator")
@Description("Checks whether a player is a server operator.")
@Example("player is an operator")
@Since("2.7")
public class CondIsOp extends PropertyCondition<OfflinePlayer> {

	static {
		register(CondIsOp.class, "[[a] server|an] op[erator][s]", "offlineplayers");
	}

	@Override
	public boolean check(OfflinePlayer player) {
		return player.isOp();
	}

	@Override
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(OfflinePlayer player, boolean op, ChangeMode mode) {
		player.setOp(op);
	}

	@Override
	protected String getPropertyName() {
		return "op";
	}

}
