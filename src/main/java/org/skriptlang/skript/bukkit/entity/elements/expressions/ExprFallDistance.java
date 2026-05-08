package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fall Distance")
@Description({"The distance an entity has fallen for."})
@Example("set all entities' fall distance to 10")
@Example("""
	on damage:
		send "%victim's fall distance%" to victim
	""")
@Since("2.5")
public class ExprFallDistance extends SimplePropertyExpression<Entity, Number> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprFallDistance.class, Number.class, "fall[en] (distance|height)", "entities", false)
				.supplier(ExprFallDistance::new)
				.build()
		);
	}

	@Override
	public @Nullable Number convert(Entity entity) {
		return entity.getFallDistance();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta != null) {
			Entity[] entities = getExpr().getArray(event);
			float number = ((Number) delta[0]).floatValue();
			if (mode == ChangeMode.SET) {
				number = Math2.fit(0, number, Float.MAX_VALUE);
			} else if (mode == ChangeMode.REMOVE) {
				number = -number;
			}
			for (Entity entity : entities) {
				float current = entity.getFallDistance();
				switch (mode) {
					case SET -> entity.setFallDistance(number);
					case ADD, REMOVE -> entity.setFallDistance(Math2.fit(0, current + number, Float.MAX_VALUE));
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
