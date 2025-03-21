package ch.njol.skript.expressions;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * TODO actually allow to have triggers execute for cancelled events
 * 
 * @author Peter Güttinger
 */
@NoDoc
public class ExprEventCancelled extends SimpleExpression<Boolean> {
	static {
		Skript.registerExpression(ExprEventCancelled.class, Boolean.class, ExpressionType.SIMPLE, "[is] event cancelled");
	}
	
	@SuppressWarnings("null")
	private Kleenean delay;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		delay = isDelayed;
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	@Nullable
	protected Boolean[] get(final Event e) {
		if (!(e instanceof Cancellable))
			return new Boolean[0];
		return new Boolean[] {((Cancellable) e).isCancelled()};
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "is event cancelled";
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (delay != Kleenean.FALSE) {
			Skript.error("Can't cancel the event anymore after it has already passed");
			return null;
		}
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Boolean.class);
		return null;
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (!(e instanceof Cancellable))
			return;
		switch (mode) {
			case DELETE:
				((Cancellable) e).setCancelled(false);
				break;
			case SET:
				assert delta != null;
				((Cancellable) e).setCancelled((Boolean) delta[0]);
		}
	}
	
}
