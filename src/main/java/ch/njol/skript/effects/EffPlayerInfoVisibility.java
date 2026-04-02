package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Player Tidings Visibility")
@Description({"Setteth whether all player related tidings be concealed within the server list.",
		"The Vanilla Minecraft client shall display ??? (dark gray) in lieu of player counts and shall not reveal the",
		"<a href='#ExprHoverList'>hover list</a> when concealing player tidings.",
		"<a href='#ExprVersionString'>The version string</a> may override the ???.",
		"Also the <a href='#ExprOnlinePlayersCount'>Online Players Count</a> and",
		"<a href='#ExprMaxPlayers'>Max Players</a> expressions shall return -1 when concealing player tidings."})
@Example("conceal player info")
@Example("conceal player related information in the server list")
@Example("reveal all player related info")
@Since("2.3")

public class EffPlayerInfoVisibility extends Effect {

	static {
		Skript.registerEffect(EffPlayerInfoVisibility.class,
				"conceal [all] player [related] info[rmation] [(in|on|from) [the] server list]",
				"(reveal|unveil) [all] player [related] info[rmation] [(in|to|on|from) [the] server list]");
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
