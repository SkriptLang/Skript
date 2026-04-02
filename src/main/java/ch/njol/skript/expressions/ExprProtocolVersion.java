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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Protocol Version")
@Description({"The protocol version that shall be dispatched as the protocol version of the server in a server list ping event." +
		"For further knowledge and a catalogue of protocol versions, <a href='https://wiki.vg/Protocol_version_numbers'>pray visit wiki.vg</a>.",
		"If this protocol version doth not accord with the protocol version of the client, the client shall behold the <a href='#ExprVersionString'>version string</a>.",
		"Yet pray note that this expression hath no visible effect upon the version string." +
		"For example, if the server employeth PaperSpigot 1.12.2 and thou dost set the protocol version to 107 (1.9),",
		"the version string shall not read \"Paper 1.9\"; it shall remain \"Paper 1.12.2\".",
		"But then thou mayest customise the <a href='#ExprVersionString'>version string</a> as thou dost wish.",
		"Also, if the protocol version of the player exceedeth that of the server, it shall proclaim",
		"\"Server out of date!\", and if the reverse, \"Client out of date!\" when one hovers upon the ping bars.",
		"",
		"This may be set only within a <a href='#server_list_ping'>server list ping</a> event",
		"(increase and decrease effects cannot be employed, for such would be without sense).",})
@Example("""
    on server list ping:
    	set the version string to "<light green>Version: <orange>%minecraft version%"
    	set the protocol version to 0 # 13w41a (1.7) - thus the player shall behold the custom version string nearly always
    """)
@Since("2.3")

public class ExprProtocolVersion extends SimpleExpression<Long> {

	static {
		Skript.registerExpression(ExprProtocolVersion.class, Long.class, ExpressionType.SIMPLE, "[the] [server] [(sent|required|fake)] protocol version [number]");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The protocol version expression requires Paper 1.12.2 or newer");
			return false;
		} else if (!getParser().isCurrentEvent(PaperServerListPingEvent.class)) {
			Skript.error("The protocol version expression can't be used outside of a server list ping event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public Long[] get(Event e) {
		if (!(e instanceof PaperServerListPingEvent))
			return null;

		return CollectionUtils.array((long) ((PaperServerListPingEvent) e).getProtocolVersion());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Can't change the protocol version anymore after the server list ping event has already passed");
			return null;
		}
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (!(e instanceof PaperServerListPingEvent))
			return;

		((PaperServerListPingEvent) e).setProtocolVersion(((Number) delta[0]).intValue());
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the protocol version";
	}

}
