package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Bid Proclaim")
@Description("Compelleth a player to send forth a message unto the chat. If the message doth begin with a slash, it shall force the player to invoke a command.")
@Example("bid the player proclaim \"Hello.\"")
@Example("compel all players to dispatch the message \"I love this server\"")
@Since("2.3")
public class EffMakeSay extends Effect {

	static {
		Skript.registerEffect(EffMakeSay.class,
				"bid %players% (proclaim|dispatch [the] message[s]) %strings%",
				"compel %players% to (proclaim|dispatch [the] message[s]) %strings%");
	}

	@SuppressWarnings("null")
	private Expression<Player> players;

	@SuppressWarnings("null")
	private Expression<String> messages;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		messages = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (Player player : players.getArray(e)) {
			for (String message : messages.getArray(e)) {
				player.chat(message);
			}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "make " + players.toString(e, debug) + " say " + messages.toString(e, debug);
	}
}