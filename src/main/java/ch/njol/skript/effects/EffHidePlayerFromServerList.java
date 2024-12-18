package ch.njol.skript.effects;

import java.util.Arrays;
import java.util.Iterator;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.Nullable;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.google.common.collect.Iterators;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Hide Player from Server List")
@Description({"Hides a player from the <a href='expressions.html#ExprHoverList'>hover list</a> " +
		"and decreases the <a href='expressions.html#ExprOnlinePlayersCount'>online players count</a> (only if the player count wasn't changed before)."})
@Examples({"on server list ping:",
		"	hide {vanished::*} from the server list"})
@Since("2.3")
public class EffHidePlayerFromServerList extends Effect {

	static {
		Skript.registerEffect(EffHidePlayerFromServerList.class,
				"hide %players% (in|on|from) [the] server list",
				"hide %players%'[s] info[rmation] (in|on|from) [the] server list");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	@SuppressWarnings("null")
	private Expression<Player> players;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean isServerPingEvent = getParser().isCurrentEvent(ServerListPingEvent.class) ||
				(PAPER_EVENT_EXISTS && getParser().isCurrentEvent(PaperServerListPingEvent.class));
		if (!isServerPingEvent) {
			Skript.error("The hide player from server list effect can't be used outside of a server list ping event");
			return false;
		} else if (isDelayed == Kleenean.TRUE) {
			Skript.error("Can't hide players from the server list anymore after the server list ping event has already passed");
			return false;
		}
		players = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event e) {
		if (!(e instanceof ServerListPingEvent))
			return;

		Iterator<Player> it = ((ServerListPingEvent) e).iterator();		
		Iterators.removeAll(it, Arrays.asList(players.getArray(e)));
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "hide " + players.toString(e, debug) + " from the server list";
	}

}