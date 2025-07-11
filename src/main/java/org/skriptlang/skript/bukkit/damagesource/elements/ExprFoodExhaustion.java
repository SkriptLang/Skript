package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperimentSyntax;

@Name("Damage Source - Food Exhaustion")
@Description("The amount of hunger exhaustion caused by a damage source.")
@Example("""
	on damage:
		if the food exhaustion of event-damage source is 10:
	""")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20.4+")
@SuppressWarnings("UnstableApiUsage")
public class ExprFoodExhaustion extends SimplePropertyExpression<DamageSource, Float> implements DamageSourceExperimentSyntax {

	static {
		registerDefault(ExprFoodExhaustion.class, Float.class, "food exhaustion", "damagesources");
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
