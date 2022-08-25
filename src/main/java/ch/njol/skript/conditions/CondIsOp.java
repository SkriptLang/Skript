package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.OfflinePlayer;

@Name("Is Op")
@Description("Checks whether a player is a server operator.")
@Examples("player is an op")
@Since("INSERT VERSION")
public class CondIsOp extends PropertyCondition<OfflinePlayer> {

	static {
		register(CondIsOp.class, PropertyType.BE, "a[n] [server] op[erator]", "offlineplayers");
	}

	@Override
	public boolean check(OfflinePlayer player) {
		return  player.isOp();
	}

	@Override
	protected String getPropertyName() {
		return "op";
	}

}