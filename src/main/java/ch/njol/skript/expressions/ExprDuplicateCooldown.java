package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Allay Duplication Cooldown")
@Description({
	"The cooldown time until an allay can duplicate again naturally.",
	"Resetting the cooldown time will set the cooldown time to the same amount of time after an allay has duplicated."
})
@Examples({
	"set {_time} to the duplicate cooldown of last spawned allay",
	"add 5 seconds to the duplication cool down time of last spawned allay",
	"remove 3 seconds from the duplicating cooldown time of last spawned allay",
	"clear the clone cool down of last spawned allay",
	"reset the cloning cool down time of last spawned allay"
})
@Since("INSERT VERSION")
public class ExprDuplicateCooldown extends SimplePropertyExpression<LivingEntity, Timespan> {

	static {
		registerDefault(ExprDuplicateCooldown.class, Timespan.class, "(duplicat(e|ing|ion)|clon(e|ing)) cool[ ]down [time]", "livingentities");
	}

	@Override
	public @Nullable Timespan convert(LivingEntity entity) {
		if (entity instanceof Allay allay)
			return new Timespan(TimePeriod.TICK, allay.getDuplicationCooldown());
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, DELETE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		long ticks = delta == null ? 0 : ((Timespan) delta[0]).getAs(TimePeriod.TICK);
		ticks = Math2.fit(0, ticks, Long.MAX_VALUE);
		for (LivingEntity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Allay allay))
				continue;
			switch (mode) {
				case SET, DELETE -> allay.setDuplicationCooldown(ticks);
				case ADD -> {
					long current = allay.getDuplicationCooldown();
					long value = Math2.fit(0, current + ticks, Long.MAX_VALUE);
					allay.setDuplicationCooldown(value);
				}
				case REMOVE -> {
					long current = allay.getDuplicationCooldown();
					long value = Math2.fit(0, current - ticks, Long.MAX_VALUE);
					allay.setDuplicationCooldown(value);
				}
				case RESET -> allay.resetDuplicationCooldown();
			}
		}
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "duplicate cooldown time";
	}

}
