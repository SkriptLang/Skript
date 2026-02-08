package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Max Health")
@Description("The maximum health of an entity, e.g. 10 for a player.")
@Example("""
	on join:
		set the maximum health of the player to 100
	""")
@Example("""
	spawn a giant
	set the last spawned entity's max health to 1000
	""")
@Since("2.0")
@Events({"damage", "death"})
public class ExprMaxHealth extends SimplePropertyExpression<LivingEntity, Number> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprMaxHealth.class, Number.class, "max[imum] health", "livingentities", false)
				.supplier(ExprMaxHealth::new)
				.build()
		);
	}
	
	@Override
	public Number convert(LivingEntity entity) {
		return HealthUtils.getMaxHealth(entity);
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "max health";
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
		double health = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		for (LivingEntity entity : getExpr().getArray(event)) {
			switch (mode) {
				case SET -> HealthUtils.setMaxHealth(entity, health);
				case RESET -> entity.resetMaxHealth();
				case ADD -> HealthUtils.setMaxHealth(entity, HealthUtils.getMaxHealth(entity) + health);
				case REMOVE -> HealthUtils.setMaxHealth(entity, HealthUtils.getMaxHealth(entity) - health);
			}
		}
	}
	
}
