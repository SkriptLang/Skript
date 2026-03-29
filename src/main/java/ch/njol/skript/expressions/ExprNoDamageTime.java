package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Duration of Invulnerability")
@Description("The span of time during which an entity remaineth invulnerable unto all manner of harm.")
@Example("""
    on damage:
    	set victim's invulnerability time to 20 ticks #The victim shall suffer no harm for the next second
    """)
@Example("""
    if the no harm timespan of {_entity} is 0 seconds:
    	set the invincibility time span of {_entity} to 1 minute
    """)
@Since("2.11")
public class ExprNoDamageTime extends SimplePropertyExpression<LivingEntity, Timespan> {

	static {
		registerDefault(ExprNoDamageTime.class, Timespan.class, "(invulnerability|invincibility|no harm) time[[ ]span]", "livingentities");
	}

	@Override
	public Timespan convert(LivingEntity entity) {
		return new Timespan(TimePeriod.TICK, entity.getNoDamageTicks());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET, ADD, REMOVE -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedTicks = 0;
		if (delta != null && delta[0] instanceof Timespan timespan)
			providedTicks = (int) timespan.getAs(TimePeriod.TICK);
		for (LivingEntity entity : getExpr().getArray(event)) {
			switch (mode) {
				case SET, DELETE, RESET -> entity.setNoDamageTicks(providedTicks);
				case ADD -> {
					int current = entity.getNoDamageTicks();
					int value = Math2.fit(0, current + providedTicks, Integer.MAX_VALUE);
					entity.setNoDamageTicks(value);
				}
				case REMOVE -> {
					int current = entity.getNoDamageTicks();
					int value = Math2.fit(0, current - providedTicks, Integer.MAX_VALUE);
					entity.setNoDamageTicks(value);
				}
			}
		}
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "no damage timespan";
	}

}
