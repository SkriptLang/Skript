package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.item.Equippable;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Equipment Slot")
@Description("""
	The equipment slot an item can be equipped to.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set the equipment slot of {_item} to chest slot")
@Example("""
	set {_component} to the equippable component of {_item}
	set the equipment slot of {_component} to boots slot
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class ExprEquipCompSlot extends SimplePropertyExpression<EquippableWrapper, EquipmentSlot> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEquipCompSlot.class, EquipmentSlot.class, "equipment slot", "equippablecomponents", true)
				.supplier(ExprEquipCompSlot::new)
				.build()
		);
	}

	@Override
	public @Nullable EquipmentSlot convert(EquippableWrapper wrapper) {
		return wrapper.getComponent().slot();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(EquipmentSlot.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		EquipmentSlot providedSlot = (EquipmentSlot) delta[0];

		getExpr().stream(event).forEach(wrapper -> {
			Equippable changed = wrapper.clone(providedSlot);
			wrapper.applyComponent(changed);
		});
	}

	@Override
	public Class<EquipmentSlot> getReturnType() {
		return EquipmentSlot.class;
	}

	@Override
	protected String getPropertyName() {
		return "equipment slot";
	}

}
