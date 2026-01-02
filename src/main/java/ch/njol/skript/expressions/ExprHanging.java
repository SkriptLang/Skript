package ch.njol.skript.expressions;

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
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.jetbrains.annotations.Nullable;

@Name("Hanging Entity/Remover")
@Description("Returns the hanging entity or remover in hanging <a href='#break_mine'>break</a> and <a href='#place'>place</a> events.")
@Example("""
	on break of item frame:
		if item of hanging entity is diamond pickaxe:
			cancel event
			if hanging remover is a player:
				send "You can't break that item frame!" to hanging remover
	""")
@Since("2.6.2")
public class ExprHanging extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprHanging.class, Entity.class, ExpressionType.SIMPLE, "[the] hanging (entity|:remover)");
	}

	private boolean isRemover;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isRemover = parseResult.hasTag("remover");

		if (isRemover && !getParser().isCurrentEvent(HangingBreakEvent.class)) {
			Skript.error("The expression 'hanging remover' can only be used in break event");
			return false;
		} else if (!getParser().isCurrentEvent(HangingBreakEvent.class, HangingPlaceEvent.class)) {
			Skript.error("The expression 'hanging entity' can only be used in break and place events");
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	public Entity[] get(Event e) {
		if (!(e instanceof HangingEvent))
			return null;

		Entity entity = null;

		if (!isRemover)
			entity = ((HangingEvent) e).getEntity();
		else if (e instanceof HangingBreakByEntityEvent)
			entity = ((HangingBreakByEntityEvent) e).getRemover();

		return new Entity[] { entity };
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}
	
	@Override
	@SuppressWarnings("null")
	public String toString(@Nullable Event e, boolean debug) {
		return "hanging " + (isRemover ? "remover" : "entity");
	}
	
}
