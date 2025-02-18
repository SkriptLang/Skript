package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Version String")
@Description({
	"The text to show if the protocol version of the server doesn't match with protocol version of the client. " +
	"You can check the <a href='#ExprProtocolVersion'>protocol version</a> expression for more information about this.",
	"This can only be set in a <a href='events.html#server_list_ping'>server list ping</a> event."
})
@Examples({
	"on server list ping:",
		"\tset the protocol version to 0 # 13w41a (1.7), so it will show the version string always",
		"\tset the version string to \"&lt;light green&gt;Version: &lt;orange&gt;%minecraft version%\""
})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2+")
@Events("Server List Ping")
public class ExprVersionString extends SimpleExpression<String> {

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	static {
		Skript.registerExpression(ExprVersionString.class, String.class, ExpressionType.SIMPLE, "[the] [shown|custom] version [string|text]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The 'version string' expression requires Paper 1.12.2+");
			return false;
		} else if (!getParser().isCurrentEvent(PaperServerListPingEvent.class)) {
			Skript.error("The 'version string' expression can't be used outside of a 'server list ping' event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public String[] get(Event event) {
		if (!(event instanceof PaperServerListPingEvent))
			return new String[0];
		return CollectionUtils.array(((PaperServerListPingEvent) event).getVersion());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Can't change the version string anymore after the server list ping event has already passed");
			return null;
		}
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof PaperServerListPingEvent))
			return;
		((PaperServerListPingEvent) event).setVersion(((String) delta[0]));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the version string";
	}

}