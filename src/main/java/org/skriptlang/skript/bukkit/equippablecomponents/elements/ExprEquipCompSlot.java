package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableExperiment;
import org.skriptlang.skript.bukkit.equippablecomponents.EquippableWrapper;

@Name("Equippable Component - Equipment Slot")
@Description("The equipment slot an item can be equipped to. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Examples({
	"set the equipment slot of {_item} to chest slot",
	"",
	"set {_component} to the equippable component of {_item}",
	"set the equipment slot of {_component} to boots slot",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompSlot extends PropertyExpression<EquippableWrapper, EquipmentSlot> implements EquippableExperiment {

	static {
		register(ExprEquipCompSlot.class, EquipmentSlot.class, "equipment slot", "equippablecomponents");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<EquippableWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected EquipmentSlot[] get(Event event, EquippableWrapper[] source) {
		return get(source, wrapper -> wrapper.getComponent().getSlot());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(EquipmentSlot.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;
		EquipmentSlot providedSlot = (EquipmentSlot) delta[0];
		if (providedSlot == null)
			return;

		for (EquippableWrapper wrapper : getExpr().getArray(event)) {
			EquippableComponent component = wrapper.getComponent();
			component.setSlot(providedSlot);
			wrapper.applyComponent();
		}
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<EquipmentSlot> getReturnType() {
		return EquipmentSlot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the equipment slot of " + getExpr().toString(event, debug);
	}

}
