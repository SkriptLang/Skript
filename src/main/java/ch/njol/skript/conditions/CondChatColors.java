package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import com.destroystokyo.paper.ClientOption;
import org.bukkit.entity.Player;

@Name("Can See Chat Colors")
@Description("Checks whether a player can see chat colors.")
@Examples({
	"if player can see chat colors:",
		"\tsend \"Find the red word in <red>this<reset> message.\"",
	"else:",
		"\tsend \"You cannot partake in finding the colored word.\""
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class CondChatColors extends PropertyCondition<Player> {

	static {
		if (Skript.classExists("com.destroystokyo.paper.ClientOption"))
			register(CondChatColors.class, PropertyType.CAN, "see chat colo[u]r[s|ing]", "players");
	}

	@Override
	public boolean check(Player player) {
		return player.getClientOption(ClientOption.CHAT_COLORS_ENABLED);
	}

	@Override
	protected String getPropertyName() {
		return "see chat colors";
	}

}
