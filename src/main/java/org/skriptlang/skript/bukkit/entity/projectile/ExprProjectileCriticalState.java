package org.skriptlang.skript.bukkit.entity.projectile;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Projectile Critical State")
@Description("A projectile's critical state. The only currently accepted projectiles are arrows and tridents.")
@Example("""
	on shoot:
		event-projectile is an arrow
		set projectile critical mode of event-projectile to true
	""")
@Since("2.5.1")
public class ExprProjectileCriticalState extends SimplePropertyExpression<Projectile, Boolean> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprProjectileCriticalState.class,
				Boolean.class,
				"(projectile|arrow) critical (state|ability|mode)",
				"projectiles",
				false
			).supplier(ExprProjectileCriticalState::new)
				.build()
		);
	}

	@Override
	public @Nullable Boolean convert(Projectile projectile) {
		return projectile instanceof AbstractArrow abstractArrow ? abstractArrow.isCritical() : null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.SET) ? CollectionUtils.array(Boolean.class) : null;
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		boolean state = (Boolean) delta[0];
		for (Projectile entity : getExpr().getAll(event)) {
			if (entity instanceof AbstractArrow arrow)
				arrow.setCritical(state);
		}
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "critical arrow state";
	}
	
}
