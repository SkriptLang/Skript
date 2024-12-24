package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Pickup Delay")
@Description("The amount of time before a dropped item can be picked up by an entity.")
@Examples({
	"drop diamond sword at {_location} without velocity",
	"set pickup delay of last dropped item to 5 seconds"
})
@Since("2.7")
public class ExprPickupDelay extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprPickupDelay.class, Timespan.class, "pick[ ]up delay", "entities");
	}

	@Override
	@Nullable
	public Timespan convert(Entity entity) {
		if (!(entity instanceof Item))
			return null;
		return new Timespan(Timespan.TimePeriod.TICK, ((Item) entity).getPickupDelay());
	}


	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case RESET:
			case DELETE:
			case REMOVE:
				return CollectionUtils.array(Timespan.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Entity[] entities = getExpr().getArray(event);
		int change = delta == null ? 0 : (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
		switch (mode) {
			case REMOVE:
				change = -change;
			case ADD:
				for (Entity entity : entities) {
					if (entity instanceof Item) {
						Item item = (Item) entity;
						item.setPickupDelay(item.getPickupDelay() + change);
					}
				}
				break;
			case DELETE:
			case RESET:
			case SET:
				for (Entity entity : entities) {
					if (entity instanceof Item)
						((Item) entity).setPickupDelay(change);
				}
				break;
			default:
				assert false;
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "pickup delay";
	}

}
