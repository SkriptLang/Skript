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

@Name("Damage Reduction - Angle")
@Description("""
	The angle in which the item can block the attack.
	The angle is dependant on the direction the player is looking when blocking.
	The direction the player is looking is considered 0 degrees. If the angle is set to 90, then the area that can be blocked \
	is 45 degrees left and 45 degrees right around the player, from where the player is looking.
	Damage Reductions contain data that attribute to:
		- What damage types can be being blocked
		- The base amount of damage to block when blocking one of the damage types
		- The factor amount of damage to block when blocking one of the damage types
		- The angle at which the item can block when blocking one of the damage types
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_reductions::*} to the damage reductions of {_item}
	set {_angles::*} to the reduction angles of {_reductions::*}
	""")
@Example("set the damage reduction angles of (the damage reductions of {_item}) to 100")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprReductionAngle extends SimplePropertyExpression<DamageReductionWrapper, Float> implements BlockingExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprReductionAngle.class,
				Float.class,
				"[damage] reduction [block[ing]] angle[s]",
				"damagereductions",
				true
			)
				.supplier(ExprReductionAngle::new)
				.build()
		);
	}

	@Override
	public @Nullable Float convert(DamageReductionWrapper wrapper) {
		return wrapper.getDamageReduction().horizontalBlockingAngle();
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
			float current = wrapper.getDamageReduction().horizontalBlockingAngle();
			switch (mode) {
				case SET, RESET -> current = provided;
				case ADD -> current += provided;
				case REMOVE -> current -= provided;
			}
			float newAngle = Math.abs(current);
			wrapper.modify(builder -> builder.horizontalBlockingAngle(newAngle));
		});
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "damage reduction angle";
	}

}
