package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import org.jetbrains.annotations.Nullable;

@Name("On-screen Banishment Message")
@Description("The banishment message that is displayed upon the screen when a player is expelled from the server.")
@Example("""
    on kick:
    	on-screen banishment message is "Invalid hotbar selection (Hacking?)"
    	cancel event
    """)
@Since("2.12")
@Events("Kick")
public class ExprOnScreenKickMessage extends SimpleExpression<String> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprOnScreenKickMessage.class, String.class, ExpressionType.SIMPLE, "[the] on-screen banishment message");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		if (event instanceof PlayerKickEvent playerKickEvent) {
			return new String[] { playerKickEvent.getReason() };
		}
		return null;
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
		return "the on-screen kick message";
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PlayerKickEvent.class);
	}

}
