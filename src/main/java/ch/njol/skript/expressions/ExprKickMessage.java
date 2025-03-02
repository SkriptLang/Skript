package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import org.jetbrains.annotations.Nullable;

@Name("Kick Message")
@Description("The kick message that is displayed on-screen when a player is kicked.")
@Examples({
	"on kick:",
		"\tkick message is 'Invalid hotbar selection (Hacking?)'",
		"\tcancel event"
})
@Since("INSERT VERSION")
@Events("Kick")
public class ExprKickMessage extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprKickMessage.class, String.class, ExpressionType.SIMPLE, "kick (message|reason)");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		if (event instanceof PlayerKickEvent playerKickEvent) {
			return new String[] { playerKickEvent.getReason() };
		}
		return new String[0];
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET ? CollectionUtils.array(String.class) : null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		assert delta.length == 1;
		if (event instanceof PlayerKickEvent kickEvent && delta[0] instanceof String text) {
			kickEvent.setReason(text);
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
