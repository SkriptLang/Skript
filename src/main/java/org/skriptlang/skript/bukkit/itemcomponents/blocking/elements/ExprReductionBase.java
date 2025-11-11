package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.DamageReductionWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Damage Reduction - Base Amount")
@Description("""
	The base amount of damage to block when the item blocks an attack.
	Damage Reductions contain data that attribute to:
		- What damage types can be being blocked
		- The base amount of damage to block when blocking one of the damage types
		- The factor amount of damage to block when blocking one of the damage types
		- The angle at which the item can block when blocking one of the damage types
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_reductions::*} to the damage reductions of {_item}
	set {_amounts::*} to the reduction base amounts of {_reductions::*}
	""")
@Example("set the damage reduction base of (the damage reductions of {_item}) to 100")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprReductionBase extends SimplePropertyExpression<DamageReductionWrapper, Float> implements BlockingExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprReductionBase.class,
				Float.class,
				"[damage] reduction base [amount[s]]",
				"damagereductions",
				true
			)
				.supplier(ExprReductionBase::new)
				.build()
		);
	}

	@Override
	public @Nullable Float convert(DamageReductionWrapper wrapper) {
		return wrapper.getDamageReduction().base();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD, RESET -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		float provided = delta == null ? 0f : ((Number) delta[0]).floatValue();

		getExpr().stream(event).forEach(wrapper -> {
			float current = wrapper.getDamageReduction().base();
			switch (mode) {
				case SET, RESET -> current = provided;
				case ADD -> current += provided;
				case REMOVE -> current -= provided;
			}
			float newBase = current;
			wrapper.modify(builder -> builder.base(newBase));
		});
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "damage reduction base amount";
	}

}
