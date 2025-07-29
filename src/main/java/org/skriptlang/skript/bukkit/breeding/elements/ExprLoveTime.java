package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Love Time")
@Description({
	"The amount of time the animals have been in love for. " +
	"Using a value of 30 seconds is equivalent to using an item to breed them.",
	"Only works on animals that can be bred and returns '0 seconds' for animals that can't be bred."
})
@Examples({
	"on right click:",
		"\tsend \"%event-entity% has been in love for %love time of event-entity% more than you!\" to player"
})
@Since("2.10")
public class ExprLoveTime extends SimplePropertyExpression<LivingEntity, Timespan> {

	static {
		register(ExprLoveTime.class, Timespan.class, "love[d] time", "livingentities");
	}

	@Override
	public @Nullable Timespan convert(LivingEntity entity) {
		if (entity instanceof Animals animal)
			return new Timespan(Timespan.TimePeriod.TICK, animal.getLoveModeTicks());
		
		return new Timespan(0);
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> CollectionUtils.array(Timespan.class);
			case ADD, REMOVE -> CollectionUtils.array(Timespan[].class);
			case RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int changeTicks = 0;

		if (delta != null) {
			for (Object object : delta) {
				changeTicks += (int) ((Timespan) object).getAs(Timespan.TimePeriod.TICK);
			}
		}

		for (LivingEntity livingEntity : getExpr().getArray(event)) {
			if (!(livingEntity instanceof Animals animal))
				continue;

			int loveTicks = animal.getLoveModeTicks();
			switch (mode) {
				case ADD -> loveTicks += changeTicks;
				case REMOVE -> loveTicks -= changeTicks;
				case SET -> loveTicks = changeTicks;
				case RESET -> loveTicks = 0;
			}
			animal.setLoveModeTicks(Math.max(loveTicks, 0));
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "love time";
	}

}
