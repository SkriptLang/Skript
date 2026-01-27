package org.skriptlang.skript.bukkit.entity.general.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Gravity")
@Description("If entity is affected by gravity or not, i.e. if it has Minecraft 1.10+ NoGravity flag.")
@Example("set gravity of player off")
@Since("2.2-dev21")
public class ExprGravity extends SimplePropertyExpression<Entity, Boolean> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprGravity.class, Boolean.class, "gravity", "entities", false)
				.supplier(ExprGravity::new)
				.build()
		);
	}
	
	@Override
	public Boolean convert(Entity entity) {
		return entity.hasGravity();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return new Class[] {Boolean.class};
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		boolean state = delta == null ? false : (boolean) delta[0];
		for (Entity entity : getExpr().getArray(event))
			entity.setGravity(state);
	}

	@Override
	protected String getPropertyName() {
		return "gravity";
	}

	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}

}
