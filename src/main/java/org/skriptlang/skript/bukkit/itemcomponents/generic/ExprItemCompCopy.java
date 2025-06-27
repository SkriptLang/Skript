package org.skriptlang.skript.bukkit.itemcomponents.generic;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;

@Name("Item Component - Copy")
@Description("Grab a copy of an item component of an item. Any changes made to the copy will not be present on the item.")
@Example("set {_component} to the copied equippable component of {_item}")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21.2+")

@SuppressWarnings("rawtypes")
public class ExprItemCompCopy extends SimplePropertyExpression<ComponentWrapper, ComponentWrapper> {

	static {
		register(ExprItemCompCopy.class, ComponentWrapper.class,
			"(([the|a] copy|[the] copies)) [component[s]]", "itemcomponents");
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
		return "the copies";
	}

}
