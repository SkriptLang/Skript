package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTameEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("The Tamer")
@Description("The one who hath tamed a creature. May only be employed within entity tame events. Thou mayest use 'event-entity' to refer to the tamed beast itself.")
@Example("""
    on tame:
    	if the tamer is a player:
    		send "someone hath tamed a creature!" to console
    """)
@Since("2.2-dev25")
public class ExprTamer extends SimpleExpression<Player> {
	
	static {
		Skript.registerExpression(ExprTamer.class, Player.class, ExpressionType.SIMPLE, "[the] tamer");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (!getParser().isCurrentEvent(EntityTameEvent.class)) {
			Skript.error("the expression 'tamer' may only be used in the entity tame event.");
			return false;
		}
		return true;
	}
	
	@Override
	protected Player[] get(final Event e) {
		if (!(e instanceof EntityTameEvent))
			return null;

		return new Player[] {((EntityTameEvent) e).getOwner() instanceof Player ? (Player) ((EntityTameEvent) e).getOwner() : null};
	}
	
	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the tamer";
	}
}
