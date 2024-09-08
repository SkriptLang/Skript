package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Name("Hover List")
@Description({
	"The list when you hover on the player counts of the server in the server list.",
	"This can be changed using texts or players in a <a href='events.html#server_list_ping'>server list ping</a> event only. " +
	"Adding players to the list means adding the name of the players.",
	"And note that, for example if there are 5 online players (includes <a href='#ExprOnlinePlayersCount'>fake online count</a>) " +
	"in the server and the hover list is set to 3 values, Minecraft will show \"... and 2 more ...\" at end of the list."
})
@Examples({
	"on server list ping:",
		"\tclear the hover list",
		"\tadd \"&aWelcome to the &6Minecraft &aserver!\" to the hover list",
		"\tadd \"\" to the hover list # A blank line",
		"\tadd \"&cThere are &6%online players count% &conline players!\" to the hover list"
})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
@Events("server list ping")
public class ExprHoverList extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprHoverList.class, String.class, ExpressionType.SIMPLE,
			"[the] [custom] [player|server] (hover|sample) ([message] list|message)",
			"[the] [custom] player [hover|sample] list");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");
	private static final boolean HAS_NEW_LISTED_PLAYER_INFO = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent$ListedPlayerInfo");

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The hover list expression requires Paper 1.12.2 or newer");
			return false;
		} else if (!getParser().isCurrentEvent(PaperServerListPingEvent.class)) {
			Skript.error("The hover list expression can't be used outside of a server list ping event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public String[] get(Event event) {
		if (!(event instanceof PaperServerListPingEvent))
			return null;

		if (HAS_NEW_LISTED_PLAYER_INFO) {
			return ((PaperServerListPingEvent) event).getListedPlayers().stream()
				.map(PaperServerListPingEvent.ListedPlayerInfo::name)
				.toArray(String[]::new);
		} else {
			return ((PaperServerListPingEvent) event).getPlayerSample().stream()
				.map(PlayerProfile::getName)
				.toArray(String[]::new);
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Can't change the hover list anymore after the server list ping event has already passed");
			return null;
		}
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
			case RESET:
				return CollectionUtils.array(String[].class, Player[].class);
		}
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof PaperServerListPingEvent))
			return;

		if (HAS_NEW_LISTED_PLAYER_INFO) {
			List<PaperServerListPingEvent.ListedPlayerInfo> values = new ArrayList<>();
			if (mode != ChangeMode.DELETE && mode != ChangeMode.RESET) {
				for (Object object : delta) {
					if (object instanceof Player) {
						Player player = (Player) object;
						values.add(new PaperServerListPingEvent.ListedPlayerInfo(player.getName(), player.getUniqueId()));
					} else {
						values.add(new PaperServerListPingEvent.ListedPlayerInfo((String) object, UUID.randomUUID()));
					}
				}
			}

			List<PaperServerListPingEvent.ListedPlayerInfo> sample = ((PaperServerListPingEvent) event).getListedPlayers();
			switch (mode) {
				case SET:
					sample.clear();
					// $FALL-THROUGH$
				case ADD:
					sample.addAll(values);
					break;
				case REMOVE:
					sample.removeAll(values);
					break;
				case DELETE:
				case RESET:
					sample.clear();
					break;
			}
			return;
		}

		List<PlayerProfile> values = new ArrayList<>();
		if (mode != ChangeMode.DELETE && mode != ChangeMode.RESET) {
			for (Object object : delta) {
				if (object instanceof Player) {
					Player player = (Player) object;
					values.add(Bukkit.createProfile(player.getUniqueId(), player.getName()));
				} else {
					values.add(Bukkit.createProfile(UUID.randomUUID(), (String) object));
				}
			}
		}

		List<PlayerProfile> sample = ((PaperServerListPingEvent) event).getPlayerSample();
		switch (mode) {
			case SET:
				sample.clear();
				sample.addAll(values);
				break;
			case ADD:
				sample.addAll(values);
				break;
			case REMOVE:
				sample.removeAll(values);
				break;
			case DELETE:
			case RESET:
				sample.clear();
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the hover list";
	}

}
