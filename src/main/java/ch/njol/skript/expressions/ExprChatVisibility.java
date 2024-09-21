package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.destroystokyo.paper.ClientOption;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Name("Chat Visibility")
@Description("The chat visibility of a player.")
@Examples({
	"if chat visibility of player is hidden:",
		"\tsend actionbar \"Server restart in 5 minutes\" to player"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class ExprChatVisibility extends SimplePropertyExpression<Player, ClientOption.ChatVisibility> {

	private static final boolean SUPPORTS_CHAT_VISIBILITY =
		Skript.classExists("com.destroystokyo.paper.ClientOption$ChatVisibility");

	static {
		if (SUPPORTS_CHAT_VISIBILITY)
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
