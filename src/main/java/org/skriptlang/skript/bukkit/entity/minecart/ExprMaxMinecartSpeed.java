package org.skriptlang.skript.bukkit.entity.minecart;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Max Minecart Speed")
@Description("The maximum speed of a minecart.")
@Example("""
	on right click on minecart:
		set max minecart speed of event-entity to 1
	""")
@Since("2.5.1")
public class ExprMaxMinecartSpeed extends SimplePropertyExpression<Entity, Number> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprMaxMinecartSpeed.class, Number.class, "max[imum] minecart (speed|velocity)", "entities", false)
				.supplier(ExprMaxMinecartSpeed::new)
				.build()
		);
	}

	@Override
	public @Nullable Number convert(Entity entity) {
		return entity instanceof Minecart minecart ? minecart.getMaxSpeed() : null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET, ADD, REMOVE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		double speed = delta == null ? 0.4 : ((Number) delta[0]).doubleValue();
		for (Entity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Minecart minecart))
				continue;
			switch (mode) {
				case SET, RESET -> minecart.setMaxSpeed(speed);
				case ADD -> minecart.setMaxSpeed(minecart.getMaxSpeed() + speed);
				case REMOVE -> minecart.setMaxSpeed(minecart.getMaxSpeed() - speed);
			}
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "max minecart speed";
	}
	
}
