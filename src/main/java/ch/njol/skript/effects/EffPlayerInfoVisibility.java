package ch.njol.skript.effects;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Player Info Visibility")
@Description({"Sets whether all player related information is hidden in the server list.",
		"The Vanilla Minecraft client will display ??? (dark gray) instead of player counts and will not show the",
		"<a href='expressions.html#ExprHoverList'>hover hist</a> when hiding player info.",
		"<a href='expressions.html#ExprVersionString'>The version string</a> can override the ???.",
		"Also the <a href='expressions.html#ExprOnlinePlayersCount'>Online Players Count</a> and",
		"<a href='expressions.html#ExprMaxPlayers'>Max Players</a> expressions will return -1 when hiding player info."})
@Examples({"hide player info",
		"hide player related information in the server list",
		"reveal all player related info"})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
@Events("server list ping")
public class EffPlayerInfoVisibility extends Effect {

	static {
		Skript.registerEffect(EffPlayerInfoVisibility.class,
				"hide [all] player [related] info[rmation] [(in|on|from) [the] server list]",
				"(show|reveal) [all] player [related] info[rmation] [(in|to|on|from) [the] server list]");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	private boolean shouldHide;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The player info visibility effect requires Paper 1.12.2 or newer");
			return false;
		} else if (!getParser().isCurrentEvent(PaperServerListPingEvent.class)) {
			Skript.error("The player info visibility effect can't be used outside of a server list ping event");
			return false;
		} else if (isDelayed == Kleenean.TRUE) {
			Skript.error("Can't change the player info visibility anymore after the server list ping event has already passed");
			return false;
		}
		shouldHide = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event e) {
		if (!(e instanceof PaperServerListPingEvent))
			return;

		((PaperServerListPingEvent) e).setHidePlayers(shouldHide);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (shouldHide ? "hide" : "show") + " player info in the server list";
	}

}