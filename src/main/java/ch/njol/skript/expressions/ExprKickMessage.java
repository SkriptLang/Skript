package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import org.jetbrains.annotations.Nullable;

@Name("Kick Message")
@Description(
	"The <a href='classes.html#kickreason'>kick message</a> as to why a player was kicked " +
	"(does not appear in chat but the kicked player's screen)<a href='events.html#kick'>kick</a> event.")
@Examples({
	"on kick:",
		"\tkick message is 'Invalid hotbar selection (Hacking?)'",
		"\tcancel event"
})
@RequiredPlugins("xxx Paper 1.16.5+")
@Since("INSERT VERSION")
@Events("Kick")
public class ExprKickMessage extends SimpleExpression<String> {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerKickEvent"))
			Skript.registerExpression(ExprKickMessage.class, String.class, ExpressionType.SIMPLE, "(disconnect|kick) message");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return getParser().isCurrentEvent(PlayerKickEvent.class);
	}

	@Override
	protected String @Nullable [] get(Event event) {
		if (event instanceof PlayerKickEvent playerKickEvent) {
			return new String[] { MiniMessage.miniMessage().serialize(playerKickEvent.reason()) };
		}
		return new String[0];
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		return mode == Changer.ChangeMode.SET ? CollectionUtils.array(String.class) : null;
	}

	@Override
	public void change(Event e, final Object[] delta, Changer.ChangeMode mode) {
		assert delta != null;
		assert delta.length == 1;
		if (e instanceof PlayerKickEvent event && delta[0] instanceof String text) {
			// avoid deprecated api
			event.reason(MiniMessage.miniMessage().deserialize(text));
		}
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
		return "the kick reason";
	}
}
