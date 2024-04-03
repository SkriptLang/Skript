package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.command.UnknownCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExprUnknownCommandMsg extends EventValueExpression<String> {

	static {
		Skript.registerExpression(ExprUnknownCommandMsg.class, String.class, ExpressionType.SIMPLE,
			"[the] [event-]unknown command [message]");
	}

	public ExprUnknownCommandMsg() {
		super(String.class);
	}


	@Override
	public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parser) {
		if (!getParser().isCurrentEvent(UnknownCommandEvent.class)){
			Skript.error("Cannot use 'unknown command message' outside of a unknown command event");
			return false;
		}
		return true;
	}

	@Override
	public @NotNull String toString(Event event, boolean b) {
		return "unknown command message";
	}

	@Override
	protected @Nullable String[] get(@NotNull Event e) {
		return new String[]{String.valueOf(((UnknownCommandEvent) e).message())};
	}

	@Override
	public void change(@NotNull Event e, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET && delta != null) {
			((UnknownCommandEvent) e).message(Component.text((String) delta[0]));
		} else {
			assert false;
		}
	}

	@Override
	public Class<?> @NotNull [] acceptChange(final Changer.@NotNull ChangeMode mode) {
		if(mode == Changer.ChangeMode.SET) {
			return CollectionUtils.array(String.class);
		}
		return null;
	}
}
