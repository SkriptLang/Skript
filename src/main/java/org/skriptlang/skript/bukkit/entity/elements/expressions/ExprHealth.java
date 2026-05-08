package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Health")
@Description("The health of a creature, e.g. a player, mob, villager, etc. The minimum value is 0, and the maximum is the creature's max health (e.g. 10 for players).")
@Example("message \"You have %health% HP left.\"")
@Since("1.0")
@Events("damage")
public class ExprHealth extends PropertyExpression<LivingEntity, Number> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprHealth.class, Number.class, "health", "livingentities", false)
				.supplier(ExprHealth::new)
				.build()
		);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		return true;
	}
	
	@Override
	protected Number[] get(Event event, LivingEntity[] source) {
		return get(source, HealthUtils::getHealth);
	}
	
	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET, ADD, REMOVE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		double change = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		for (LivingEntity entity : getExpr().getArray(event)) {
			switch (mode) {
				case SET, DELETE -> HealthUtils.setHealth(entity, change);
				case ADD -> HealthUtils.heal(entity, change);
				case REMOVE -> HealthUtils.heal(entity, -change);
				case RESET -> HealthUtils.setHealth(entity, HealthUtils.getMaxHealth(entity));
			}
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the health of " + getExpr().toString(event, debug);
	}

}
