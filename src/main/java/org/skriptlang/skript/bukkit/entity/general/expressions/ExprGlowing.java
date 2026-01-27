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

@Name("Glowing")
@Description("Indicates if targeted entity is glowing (new 1.9 effect) or not. Glowing entities can be seen through walls.")
@Example("set glowing of player to true")
@Since("2.2-dev18")
public class ExprGlowing extends SimplePropertyExpression<Entity, Boolean> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprGlowing.class, Boolean.class, "glowing", "entities", false)
				.supplier(ExprGlowing::new)
				.build()
		);
	}
	
	@Override
	public Boolean convert(Entity entity) {
		return entity.isGlowing();
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
		for (final Entity entity : getExpr().getArray(event))
			entity.setGlowing(state);
	}

	@Override
	protected String getPropertyName() {
		return "glowing";
	}

	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}

}
