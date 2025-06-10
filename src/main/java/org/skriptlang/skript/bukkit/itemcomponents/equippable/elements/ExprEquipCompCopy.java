package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;

@Name("Equippable Component - Copy")
@Description("Grab a copy of an equippable component of an item. Any changes made to the copy will not be present on the item. "
	+ "NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work aas intended.")
@Example("set {_component} to the copied equippable component of {_item}")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21.2+")

@SuppressWarnings("rawtypes")
public class ExprEquipCompCopy extends SimplePropertyExpression<ComponentWrapper, ComponentWrapper> implements EquippableExperiment {

	static {
		register(ExprEquipCompCopy.class, ComponentWrapper.class,
			"(([the|a] copy|[the] copies)|copied)", "itemcomponents");
	}

	@Override
	public @Nullable ComponentWrapper convert(ComponentWrapper wrapper) {
		return wrapper.clone();
	}

	@Override
	public Class<ComponentWrapper> getReturnType() {
		return ComponentWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "copies";
	}

}
