package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.destroystokyo.paper.ClientOption;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Name("Chat Visibility")
@Description("The chat visibility of a player. Can only be read.")
@Examples({
	"if chat visibility of player is hidden or commands only:",
		"\tsend actionbar \"Server restart in 5 minutes\" to player"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class ExprChatVisibility extends SimplePropertyExpression<Player, ClientOption.ChatVisibility> {

	static {
		if (Skript.classExists("com.destroystokyo.paper.ClientOption$ChatVisibility"))
			register(ExprChatVisibility.class, ClientOption.ChatVisibility.class,
				"chat visibility", "players");
	}

	@Override
	public @Nullable ClientOption.ChatVisibility convert(Player from) {
		return from.getClientOption(ClientOption.CHAT_VISIBILITY);
	}

	@Override
	public Class<ClientOption.ChatVisibility> getReturnType() {
		return ClientOption.ChatVisibility.class;
	}

	@Override
	protected String getPropertyName() {
		return "chat visibility";
	}

}
