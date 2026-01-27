package org.skriptlang.skript.bukkit.entity.general.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Last Damage")
@Description("The last damage that was done to an entity. Note that changing it doesn't deal more/less damage.")
@Example("set last damage of event-entity to 2")
@Since("2.5.1")
public class ExprLastDamage extends SimplePropertyExpression<LivingEntity, Number> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprLastDamage.class, Number.class, "last damage", "livingentities", false)
				.supplier(ExprLastDamage::new)
				.build()
		);
	}

	@Override
	public @Nullable Number convert(LivingEntity livingEntity) {
		return livingEntity.getLastDamage() / 2;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		double damage = ((Number) delta[0]).doubleValue() * 2;
		for (LivingEntity entity : getExpr().getArray(event)) {
			switch (mode) {
				case SET -> entity.setLastDamage(damage);
				case ADD -> entity.setLastDamage(entity.getLastDamage() + damage);
				case REMOVE -> entity.setLastDamage(entity.getLastDamage() - damage);
			}
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "last damage";
	}

}
