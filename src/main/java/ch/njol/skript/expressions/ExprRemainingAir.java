package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Remaining Air")
@Description("How much time a player has left underwater before starting to drown.")
@Examples({
	"if the player's remaining air is less than 3 seconds:",
		"\tsend \"hurry, get to the surface!\" to the player"
})
@Since("2.0")
public class ExprRemainingAir extends SimplePropertyExpression<LivingEntity, Timespan> {

	static {
		register(ExprRemainingAir.class, Timespan.class, "remaining air", "livingentities");
	}

	@Override
	public Timespan convert(LivingEntity entity) {
		/*
		 * negative values are allowed, and Minecraft itself may return a negative value from -1 to -20
		 * these negative values seem to control when the entity actually takes damage
		 * that is, when it hits -20, the entity takes damage, and it goes back to 0
		 * for simplicity, we cap it at 0 seconds (as it is still the case that the entity has no air)
		 */
		return new Timespan(TimePeriod.TICK, Math.max(0, entity.getRemainingAir()));
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case SET:
			case REMOVE:
			case DELETE:
			case RESET:
				return CollectionUtils.array(Timespan.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		// default is 15 seconds of air
		long changeValue = delta != null ? ((Timespan) delta[0]).getAs(TimePeriod.TICK) : 20 * 15;
		if (mode == ChangeMode.REMOVE) // subtract the change value
			changeValue *= -1;
		for (LivingEntity entity : getExpr().getArray(event)) {
			long newRemainingAir = 0;
			if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
				newRemainingAir = entity.getRemainingAir();
			// while entities have a "maximum air", the value is allowed to go past it
			// while negative values are permitted, the behavior is strange
			newRemainingAir = Math.max(Math.min(newRemainingAir + changeValue, Integer.MAX_VALUE), 0);
			entity.setRemainingAir((int) newRemainingAir);
		}
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "remaining air";
	}

}
