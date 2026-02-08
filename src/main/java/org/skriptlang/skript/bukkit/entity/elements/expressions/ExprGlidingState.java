package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Gliding State")
@Description("Sets of gets gliding state of player. It allows you to set gliding state of entity even if they do not have an <a href=\"https://minecraft.wiki/w/Elytra\">Elytra</a> equipped.")
@Example("set gliding of player to off")
@Since("2.2-dev21")
public class ExprGlidingState extends SimplePropertyExpression<LivingEntity, Boolean> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprGlidingState.class, Boolean.class, "(gliding|glider) [state]", "livingentities", false)
				.supplier(ExprGlidingState::new)
				.build()
		);
	}

	@Override
	public Boolean convert(LivingEntity entity) {
		return entity.isGliding();
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
		for (LivingEntity entity : getExpr().getArray(event))
			entity.setGliding(state);
	}

	@Override
	protected String getPropertyName() {
		return "gliding state";
	}

	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}

}
