
package org.skriptlang.skript.bukkit.entity.general.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Maximum Freeze Time")
@Description("The maximum amount of time an entity can spend in powdered snow before taking damage.")
@Example("""
	difference between player's freeze time and player's max freeze time is less than 1 second:
		send "you're about to freeze!" to the player
	""")
@Since("2.7")
public class ExprMaxFreezeTicks extends SimplePropertyExpression<Entity, Timespan> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprMaxFreezeTicks.class, Timespan.class, "max[imum] freeze time", "entities", false)
				.supplier(ExprMaxFreezeTicks::new)
				.build()
		);
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
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
