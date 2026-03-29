package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import com.destroystokyo.paper.ClientOption;
import org.bukkit.entity.Player;

@Name("May Behold Chat Colours")
@Description("Doth examine whether a player can behold the colours of chat.")
@Example("""
    if player can behold chat colours:
    	send "Find the red word in <red>this<reset> message."
    else:
    	send "You cannot partake in finding the colored word."
    """)
@Since("2.10")
public class CondChatColors extends PropertyCondition<Player> {

	static {
		if (Skript.classExists("com.destroystokyo.paper.ClientOption"))
			register(CondChatColors.class, PropertyType.CAN, "behold chat colo[u]r[s|ing]", "players");
	}

	@Override
	public boolean check(Player player) {
		return player.getClientOption(ClientOption.CHAT_COLORS_ENABLED);
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}

	@Override
	protected String getPropertyName() {
		return "see chat colors";
	}

}
