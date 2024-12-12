package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.ClientOption;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Can See Messages")
@Description("Checks whether a player can see specific message types in chat.")
@Examples({
	"if player can see all messages:",
		"\tsend \"You can see all messages.\"",
	"if player can only see commands:",
		"\tsend \"This game doesn't work with commands-only chat.\"",
	"if player can't see any messages:",
		"\tsend action bar \"Server shutting down in 5 minutes!\""
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class CondChatVisibility extends Condition {

	static {
		if (Skript.classExists("com.destroystokyo.paper.ClientOption$ChatVisibility"))
			Skript.registerCondition(CondChatVisibility.class,
				"%player% can see all messages [in chat]",
				"%player% can only see (commands|system messages) [in chat]",
				"%player% can('t|[ ]not) see any (command[s]|message[s]) [in chat]",
				"%player% can('t|[ ]not) see all messages [in chat]",
				"%player% can('t|[ ]not) only see (commands|system messages) [in chat]");
	}

	private int pattern = 0;
	private Expression<Player> player;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		player = (Expression<Player>) expressions[0];

		setNegated(matchedPattern > 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Player player = this.player.getSingle(event);

		if (player == null)
			return false;

		ClientOption.ChatVisibility current = player.getClientOption(ClientOption.CHAT_VISIBILITY);

		return switch (pattern) {
			case 0 -> current == ClientOption.ChatVisibility.FULL;
			case 1 -> current == ClientOption.ChatVisibility.SYSTEM;
			case 2 -> current == ClientOption.ChatVisibility.HIDDEN;
			case 3 -> current != ClientOption.ChatVisibility.FULL;
			case 4 -> current != ClientOption.ChatVisibility.SYSTEM;
			default -> throw new IllegalStateException("Unexpected value: " + pattern);
		};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (pattern) {
			case 0 -> player.toString(event, debug) + " can see all messages";
			case 1 -> player.toString(event, debug) + " can only see commands";
			case 2 -> player.toString(event, debug) + " can't see any messages";
			case 3 -> player.toString(event, debug) + " can't see all messages";
			case 4 -> player.toString(event, debug) + " can't only see commands";
			default -> throw new IllegalStateException("Unexpected value: " + pattern);
		};
	}

}
