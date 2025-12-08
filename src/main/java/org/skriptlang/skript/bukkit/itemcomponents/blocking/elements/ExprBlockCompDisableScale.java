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
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Blocking Component - Disable Cooldown Scale")
@Description("""
	The scalar applied to the disabled cooldown time for the item when disabled by an attack.
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_scale} to the disabled cooldown scale of {_item}")
@Example("set the blocking disable cooldown scale of {_item} to 2")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprBlockCompDisableScale extends SimplePropertyExpression<BlockingWrapper, Float> implements BlockingExperimentalSyntax {

	static {
		registerDefault(ExprBlockCompDisableScale.class, Float.class, "[blocking] disabl(e[d]|ing) cooldown (scale|scalar)[s]",
			"blockingcomponents");
	}

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprBlockCompDisableScale.class,
				Float.class,
				"[blocking] disabl(e[d]|ing) cooldown (scale|scalar)[s]",
				"blockingcomponents",
				true
			)
				.supplier(ExprBlockCompDisableScale::new)
				.build()
		);
	}

	@Override
	public @Nullable Float convert(BlockingWrapper wrapper) {
		return wrapper.getComponent().disableCooldownScale();
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

		getExpr().stream(event).forEach(wrapper -> {
			float current = wrapper.getComponent().disableCooldownScale();
			switch (mode) {
				case SET, DELETE -> current = provided;
				case ADD -> current += provided;
				case REMOVE -> current -= provided;
			}
			float newScale = Math2.fit(0, current, Float.MAX_VALUE);
			wrapper.editBuilder(builder -> builder.disableCooldownScale(newScale));
		});
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "blocking disable cooldown scale";
	}

}
