package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Name("Maximum Time On Fire")
@Description("Returns the maximum amount of time before an entity begins getting damaged by fire.")
@Examples({
	"send target's max fire time"
})
@Since("INSERT VERSION")
public class ExprMaxFireTicks extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprMaxFireTicks.class, Timespan.class, "max[imum] fire time", "entities");
	}

	@Override
	@Nullable
	public Timespan convert(Entity entity) {
		return Timespan.fromTicks(entity.getMaxFireTicks());
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "maximum fire time";
	}

}

