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
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.Nullable;

@Name("Server Icon")
@Description({
	"Icon of the server in the server list. Can be set to an icon that loaded using the",
	"<a href='effects.html#EffLoadServerIcon'>load server icon</a> effect,",
	"or can be reset to the default icon in a <a href='events.html#server_list_ping'>server list ping</a>.",
	"'default server icon' returns the default server icon (server-icon.png) always and cannot be changed."
})
@Examples({
	"on script load:",
		"\tset {server-icons::default} to the default server icon"
})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
public class ExprServerIcon extends SimpleExpression<CachedServerIcon> {

	private static final boolean SUPPORTS_SERVER_LIST_PING_EVENT =
		Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	static {
		Skript.registerExpression(ExprServerIcon.class, CachedServerIcon.class, ExpressionType.PROPERTY,
			"[the] [:default|shown:(shown|sent)] [server] icon");
	}

	private boolean isServerPingEvent, isDefault;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!SUPPORTS_SERVER_LIST_PING_EVENT) {
			Skript.error("The server icon expression requires Paper 1.12.2 or newer");
			return false;
		}

		isServerPingEvent = getParser().isCurrentEvent(PaperServerListPingEvent.class);
		isDefault = (parseResult.hasTag("default") && !isServerPingEvent) || parseResult.hasTag("shown");

		if (!isServerPingEvent && !isDefault) {
			Skript.error("The 'shown' server icon expression can't be used outside of a server list ping event");
			return false;
		}

		return true;
	}

	@Override
	public CachedServerIcon @Nullable [] get(Event event) {
		CachedServerIcon icon;

		if ((isServerPingEvent && !isDefault) && SUPPORTS_SERVER_LIST_PING_EVENT) {
			if (!(event instanceof PaperServerListPingEvent serverListPingEvent))
				return null;

			icon = serverListPingEvent.getServerIcon();
		} else {
			icon = Bukkit.getServerIcon();
		}

		if (icon == null || icon.getData() == null)
			return null;

		return CollectionUtils.array(icon);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isServerPingEvent || isDefault || (mode != ChangeMode.SET && mode != ChangeMode.RESET)) {
			return null;
		}

		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("The server icon can't be changed after the server list ping event has passed");
			return null;
		}

		return CollectionUtils.array(CachedServerIcon.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof PaperServerListPingEvent serverListPingEvent))
			return;

		switch (mode) {
			case SET -> serverListPingEvent.setServerIcon((CachedServerIcon) delta[0]);
			case RESET -> serverListPingEvent.setServerIcon(Bukkit.getServerIcon());
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends CachedServerIcon> getReturnType() {
		return CachedServerIcon.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + (!isServerPingEvent || isDefault ? "default" : "shown") + " server icon";
	}

}
