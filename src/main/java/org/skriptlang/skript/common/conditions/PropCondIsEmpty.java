package org.skriptlang.skript.common.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseCondition;
import org.skriptlang.skript.lang.properties.PropertyHandler.ConditionPropertyHandler;

@Name("Is Empty")
@Description("Checks whether something is empty.")
@Example("player's inventory is empty")
@Since("unknown (before 2.1)")
public class PropCondIsEmpty extends PropertyBaseCondition<ConditionPropertyHandler<?>> {

	static {
		register(PropCondIsEmpty.class, "empty", "objects");
	}

	@Override
	public @NotNull Property<ConditionPropertyHandler<?>> getProperty() {
		return Property.IS_EMPTY;
	}
}
