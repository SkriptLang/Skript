package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.DamageFunctionWrapper;

@Name("Item Damage Function - Threshold Amount")
@Description("""
	The minimum amount of damage required from blocking an attack with the item to deal the 'base' and 'factor' \
	amount of damage to the item.
	Item Damage Functions contain data that attribute to:
		- The base amount of damage to be applied to the item, if the attack damage passes the threshold
		- The factor amount to get a fraction of the attack damage to be applied to the item, if the attack damage passes the threshold
		- The threshold amount
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_amount} to the item damage function threshold amount of {_item}")
@Example("set the damage function threshold of {_item} to 100")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprDamageFunctionThreshold extends SimplePropertyExpression<Object, Float> implements BlockingExperimentalSyntax {

	static {
		registerDefault(ExprDamageFunctionFactor.class, Float.class, "[item] damage function threshold [amount[s]]",
			"blockingcomponents/itemdamagefunctions");
	}

	@Override
	public @Nullable Float convert(Object object) {
		if (object instanceof DamageFunctionWrapper wrapper) {
			return wrapper.getDamageFunction().threshold();
		} else if (object instanceof BlockingWrapper wrapper) {
			return wrapper.getComponent().itemDamage().threshold();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, REMOVE, ADD -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		float provided = delta == null ? 0f : ((Number) delta[0]).floatValue();

		getExpr().stream(event).forEach(object -> {
			DamageFunctionWrapper functionWrapper;
			if (object instanceof DamageFunctionWrapper wrapper) {
				functionWrapper = wrapper;
			} else if (object instanceof BlockingWrapper wrapper) {
				functionWrapper = wrapper.getDamageFunction();
			} else {
				return;
			}
			float current = functionWrapper.getDamageFunction().threshold();
			switch (mode) {
				case SET, DELETE -> current = provided;
				case ADD -> current += provided;
				case REMOVE -> current -= provided;
			}
			float newThreshold = Math2.fit(0, current, Float.MAX_VALUE);
			functionWrapper.modify(builder -> builder.threshold(newThreshold));
		});
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "item damage function threshold amount";
	}

}
