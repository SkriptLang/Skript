package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Entity Fire Burn Duration")
@Description("How much time an entity will be burning for.")
@Example("send \"You will stop burning in %fire time of player%\"")
@Example("send the max burn time of target")
@Since("2.7, 2.10 (maximum)")
public class ExprFireTime extends SimplePropertyExpression<Entity, Timespan> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprFireTime.class, Timespan.class, "[:max[imum]] (burn[ing]|fire) (time|duration)", "entities", false)
				.supplier(ExprFireTime::new)
				.build()
		);
	}

	private boolean max;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		max = (parseResult.hasTag("max"));
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		return new Timespan(TimePeriod.TICK, (max ? entity.getMaxFireTicks() : Math.max(entity.getFireTicks(), 0)));
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (max)
			return null;
		return switch (mode) {
			case ADD, SET, RESET, DELETE, REMOVE -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int change = delta == null ? 0 : (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
		if (mode == ChangeMode.SET) {
			change = Math2.fit(0, change, Integer.MAX_VALUE);
		} else if (mode == ChangeMode.REMOVE) {
			change = -change;
		}
		for (Entity entity : getExpr().getArray(event)) {
			switch (mode) {
				case SET, DELETE, RESET -> entity.setFireTicks(change);
				case ADD, REMOVE -> entity.setFireTicks((int) Math2.addClamped(entity.getFireTicks(), change));
			}
		}
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "fire time";
	}

}
