package org.skriptlang.skript.bukkit.damagesource.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Damage Source - Food Exhaustion")
@Description("The amount of hunger exhaustion caused by a damage source.")
@Example("""
	on damage:
		if the food exhaustion of event-damage source is 10:
	""")
@Since("2.12")
public class ExprFoodExhaustion extends SimplePropertyExpression<DamageSource, Float> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprFoodExhaustion.class,
				Float.class,
				"food exhaustion",
				"damagesources",
				true
			)
				.supplier(ExprFoodExhaustion::new)
				.build()
		);
	}

	@Override
	public @Nullable Float convert(DamageSource damageSource) {
		return damageSource.getFoodExhaustion();
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "food exhaustion";
	}

}
