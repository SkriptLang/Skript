package org.skriptlang.skript.common.properties.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.*;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseCondition;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Empty")
@Description("Checks whether something is empty.")
@Example("player's inventory is empty")
@Since("unknown (before 2.1)")
@RelatedProperty("empty")
public class PropCondIsEmpty extends PropertyBaseCondition<ConditionPropertyHandler<?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION,
			PropertyCondition.infoBuilder(
				PropCondIsEmpty.class, PropertyType.BE,
				"empty", "objects"
			)
				.supplier(PropCondIsEmpty::new)
				.build());
	}

	@Override
	public @NotNull Property<ConditionPropertyHandler<?>> getProperty() {
		return Property.IS_EMPTY;
	}

}
