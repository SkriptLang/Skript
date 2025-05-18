package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceWrapper;

//@Name("Damage Source - Food Exhaustion")
//@Description({
//	"The amount of hunger exhaustion caused by a damage source.",
//	"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event."
//})
//@Example("""
//	set {_source} to a new custom damage source:
//		set the damage type to magic
//		set the causing entity to {_player}
//		set the direct entity to {_arrow}
//		set the damage location to location(0, 0, 10)
//		set the source location to location(10, 0, 0)
//		set the food exhaustion to 10
//		make event-damage source be indirect
//		make event-damage source scale with difficulty
//	damage all players by 5 using {_source}
//	""")
//@Since("INSERT VERSION")
//@RequiredPlugins("Minecraft 1.20.4+")
public class ExprFoodExhaustion extends SimplePropertyExpression<DamageSource, Float> implements DamageSourceExperiment {

//	static {
//		registerDefault(ExprFoodExhaustion.class, Float.class, "food exhaustion", "damagesources");
//	}

	@Override
	public @Nullable Float convert(DamageSource damageSource) {
		return damageSource.getFoodExhaustion();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(Float.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Float exhaustion = delta == null ? 0f : (Float) delta[0];

		boolean hasFinal = false;
		for (DamageSource damageSource : getExpr().getArray(event)) {
			if (!(damageSource instanceof DamageSourceWrapper wrapper)) {
				hasFinal = true;
				continue;
			}
			Float current = wrapper.getFoodExhaustion();
			switch (mode) {
				case SET, DELETE -> wrapper.setFoodExhaustion(exhaustion);
				case ADD -> wrapper.setFoodExhaustion(current + exhaustion);
				case REMOVE -> wrapper.setFoodExhaustion(current - exhaustion);
			}
		}
		if (hasFinal)
			error("You cannot change the 'food exhaustion' attribute of a finalized damage source.");
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
