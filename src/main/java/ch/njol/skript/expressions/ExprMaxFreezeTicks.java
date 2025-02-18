
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Maximum Freeze Time")
@Description("The maximum amount of time an entity can spend in powdered snow before taking damage.")
@Examples({
	"difference between player's freeze time and player's max freeze time is less than 1 second:",
	"\tsend \"you're about to freeze!\" to the player"
})
@Since("2.7")
public class ExprMaxFreezeTicks extends SimplePropertyExpression<Entity, Timespan> {

	static {
		if (Skript.methodExists(Entity.class, "getMaxFreezeTicks"))
			register(ExprMaxFreezeTicks.class, Timespan.class, "max[imum] freeze time", "entities");
	}

	@Override
	@Nullable
	public Timespan convert(Entity entity) {
		return new Timespan(Timespan.TimePeriod.TICK, entity.getMaxFreezeTicks());
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "maximum freeze time";
	}

}
