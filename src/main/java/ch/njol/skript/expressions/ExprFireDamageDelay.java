package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Name("Fire Damage Delay")
@Description("Returns the amount of time before an entity begins getting damaged by fire.")
@Examples({
	"send target's fire damage delay"
})
@Since("INSERT VERSION")
public class ExprFireDamageDelay extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprFireDamageDelay.class, Timespan.class, "fire damage delay", "entities");
	}

	@Override
	@Nullable
	public Timespan convert(Entity entity) {
		return new Timespan(TimePeriod.TICK, entity.getMaxFireTicks());
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "fire damage delay";
	}

}

