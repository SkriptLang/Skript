package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceWrapper;

@Name("Damage Source - Damage Type")
@Description({
	"The type of damage of a damage source.",
	"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event."
})
@Example("""
	set {_source} to a new custom damage source:
		set the damage type to magic
		set the causing entity to {_player}
		set the direct entity to {_arrow}
		set the damage location to location(0, 0, 10)
	damage all players by 5 using {_source}
	""")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprDamageType extends SimplePropertyExpression<DamageSource, DamageType> implements DamageSourceExperiment {

	static {
		registerDefault(ExprDamageType.class, DamageType.class, "damage type", "damagesources");
	}

	@Override
	public @Nullable DamageType convert(DamageSource damageSource) {
		return damageSource.getDamageType();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(DamageType.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		DamageType damageType = (DamageType) delta[0];

		boolean hasFinal = false;
		for (DamageSource damageSource : getExpr().getArray(event)) {
			if (!(damageSource instanceof DamageSourceWrapper wrapper)) {
				hasFinal = true;
				continue;
			}
			wrapper.setDamageType(damageType);
		}
		if (hasFinal)
			error("You cannot change the 'damage type' attribute of a finalized damage source.");
	}

	@Override
	public Class<DamageType> getReturnType() {
		return DamageType.class;
	}

	@Override
	protected String getPropertyName() {
		return "damage type";
	}

}
