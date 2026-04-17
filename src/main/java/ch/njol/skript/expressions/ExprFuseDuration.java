package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fuse Duration")
@Description("The fuse duration of a creeper or primed TNT. For creepers, this is the total fuse duration before explosion. For primed TNT, this is the remaining time before explosion.")
@Example("""
	on spawn of a creeper:
		set the fuse duration of the event-entity to 5 seconds
	""")
@Example("""
	on spawn of primed tnt:
		set fuse duration of event-entity to 10 seconds
	""")
@Since("INSERT VERSION")
public class ExprFuseDuration extends SimplePropertyExpression<Entity, Timespan> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprFuseDuration.class, Timespan.class, "fuse (duration|time)", "entities", false).build()
		);
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		if (entity instanceof Creeper creeper)
			return new Timespan(Timespan.TimePeriod.TICK, creeper.getMaxFuseTicks());
		else if (entity instanceof TNTPrimed tnt)
			return new Timespan(Timespan.TimePeriod.TICK, tnt.getFuseTicks());
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int ticks = delta != null && delta[0] instanceof Timespan ts
			? (int) Math2.fit(0, ts.get(Timespan.TimePeriod.TICK), Integer.MAX_VALUE)
			: 0;
		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof Creeper creeper) {
				creeper.setMaxFuseTicks(Math2.fit(0, switch (mode) {
					case SET, DELETE -> ticks;
					case ADD -> creeper.getMaxFuseTicks() + ticks;
					case REMOVE -> creeper.getMaxFuseTicks() - ticks;
					default -> throw new IllegalArgumentException("Unexpected mode: " + mode);
				}, Integer.MAX_VALUE));
			} else if (entity instanceof TNTPrimed tnt) {
				tnt.setFuseTicks(Math2.fit(0, switch (mode) {
					case SET, DELETE -> ticks;
					case ADD -> tnt.getFuseTicks() + ticks;
					case REMOVE -> tnt.getFuseTicks() - ticks;
					default -> throw new IllegalArgumentException("Unexpected mode: " + mode);
				}, Integer.MAX_VALUE));
			}
		}
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "fuse duration";
	}

}