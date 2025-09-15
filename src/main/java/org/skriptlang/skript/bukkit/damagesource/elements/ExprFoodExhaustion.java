package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperimentSyntax;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;

@Name("Damage Source - Food Exhaustion")
@Description("The amount of hunger exhaustion caused by a damage source.")
@Example("""
	on damage:
		if the food exhaustion of event-damage source is 10:
	""")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprFoodExhaustion extends SimplePropertyExpression<DamageSource, Float> implements DamageSourceExperimentSyntax {

	public static DefaultSyntaxInfos.Expression<ExprFoodExhaustion, Float> info() {
		return DefaultSyntaxInfos.Expression.builder(ExprFoodExhaustion.class, Float.class)
				.supplier(ExprFoodExhaustion::new)
				.addPatterns(getDefaultPatterns("food exhaustion", "damagesources"))
				.build();
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
