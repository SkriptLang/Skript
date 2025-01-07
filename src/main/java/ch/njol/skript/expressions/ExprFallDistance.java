package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Fall Distance")
@Description({"The distance an entity has fallen for."})
@Examples({"set all entities' fall distance to 10",
	"on damage:",
	"\tsend \"%victim's fall distance%\" to victim"})
@Since("2.5")
public class ExprFallDistance extends SimplePropertyExpression<Entity, Number> {
	
	static {
		register(ExprFallDistance.class, Number.class, "fall[en] (distance|height)", "entities");
	}
	
	@Nullable
	@Override
	public Number convert(Entity entity) {
		return entity.getFallDistance();
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.RESET || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.DELETE) ? null : CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta != null) {
			Entity[] entities = getExpr().getArray(e);
			if (entities.length < 1)
				return;
			Float number = ((Number) delta[0]).floatValue();
			for (Entity entity : entities) {
				
				Float fallDistance = entity.getFallDistance();
				
				switch (mode) {
					case ADD:
						entity.setFallDistance(fallDistance + number);
						break;
					case SET:
						entity.setFallDistance(number);
						break;
					case REMOVE:
						entity.setFallDistance(fallDistance - number);
						break;
					default:
						assert false;
				}
			}
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "fall distance";
	}
	
}
