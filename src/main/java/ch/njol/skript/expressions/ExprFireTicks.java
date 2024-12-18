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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Fire Burn Duration")
@Description("How much time an entity will be burning for.")
@Examples({"send \"You will stop burning in %fire time of player%\""})
@Since("2.7")
public class ExprFireTicks extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprFireTicks.class, Timespan.class, "(burn[ing]|fire) (time|duration)", "entities");
	}

	@Override
	@Nullable
	public Timespan convert(Entity entity) {
		return Timespan.fromTicks(Math.max(entity.getFireTicks(), 0));
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode != ChangeMode.REMOVE_ALL) ? CollectionUtils.array(Timespan.class) :  null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Entity[] entities = getExpr().getArray(event);
		int change = delta == null ? 0 : (int) ((Timespan) delta[0]).getTicks();
		switch (mode) {
			case REMOVE:
				change = -change;
			case ADD:
				for (Entity entity : entities)
					entity.setFireTicks(entity.getFireTicks() + change);
				break;
			case DELETE:
			case RESET:
			case SET:
				for (Entity entity : entities)
					entity.setFireTicks(change);
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
		return "fire time";
	}

}
