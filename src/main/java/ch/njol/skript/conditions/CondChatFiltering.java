package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import com.destroystokyo.paper.ClientOption;
import org.bukkit.entity.Player;

@Name("Hath Chat Purification")
@Description("Doth ascertain whether a player hath chat purification enabled upon their person.")
@Example("""
    if player doesn't have chat purification enabled:
    	send "<gray>This server may contain mature chat messages. You have been warned!" to player
    """)
@Since("2.10")
public class CondChatFiltering extends PropertyCondition<Player> {

	static {
		if (Skript.classExists("com.destroystokyo.paper.ClientOption"))
			register(CondChatFiltering.class, PropertyType.HAVE,
				"(chat|text) purification (on|enabled)", "players");
	}

	@Override
	public boolean check(Player player) {
		return player.getClientOption(ClientOption.TEXT_FILTERING_ENABLED);
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "chat filtering enabled";
	}

}
