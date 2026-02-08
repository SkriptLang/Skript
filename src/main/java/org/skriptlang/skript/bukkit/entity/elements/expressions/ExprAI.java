package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.classes.Changer;
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

@Name("Entity AI")
@Description("Returns whether an entity has AI.")
@Example("set artificial intelligence of target entity to false")
@Since("2.5")
public class ExprAI extends SimplePropertyExpression<LivingEntity, Boolean> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprAI.class, Boolean.class, "(ai|artificial intelligence)", "livingentities", false)
				.supplier(ExprAI::new)
				.build()
		);
	}
	
	@Override
	public @Nullable Boolean convert(LivingEntity entity) {
		return entity.hasAI();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Boolean.class) : null;
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		assert delta != null;
		boolean value = (Boolean) delta[0];
		for (LivingEntity entity : getExpr().getArray(event)) {
			entity.setAI(value);
		}
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "artificial intelligence";
	}
	
}
