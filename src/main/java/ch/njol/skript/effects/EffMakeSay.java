package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Say")
@Description({
	"Forces a player to send a message to the chat.",
	"If the message starts with a slash it will force the player to use command."
})
@Examples({
	"make the player say \"Hello.\"",
	"force all players to send the message \"I love this server\""
})
@Since("2.3")
public class EffMakeSay extends Effect {

	static {
		Skript.registerEffect(EffMakeSay.class,
			"make %players% (say|send [the] message[s]) %strings%",
			"force %players% to (say|send [the] message[s]) %strings%");
	}

	private Expression<Player> players;
	private Expression<String> messages;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		messages = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Player player : players.getArray(event)) {
			for (String message : messages.getArray(event)) {
				player.chat(message);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + players.toString(event, debug) + " say " + messages.toString(event, debug);
	}
}