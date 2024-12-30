package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry.equipment.chances;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEquipmentWrapper.DropChance;

public class ExprSpawnerEntryEquipmentSlot extends SimplePropertyExpression<DropChance, EquipmentSlot> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntryEquipmentSlot.class, EquipmentSlot.class,
			"spawner [entry] equipment slot", "spawnerentryequipments");
	}

	@Override
	public @NotNull EquipmentSlot convert(DropChance equipment) {
		return equipment.getEquipmentSlot();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> CollectionUtils.array(EquipmentSlot.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		EquipmentSlot slot = (EquipmentSlot) delta[0];

		for (DropChance equipment : getExpr().getArray(event)) {
			equipment.setEquipmentSlot(slot);
		}
	}

	@Override
	public Class<? extends EquipmentSlot> getReturnType() {
		return EquipmentSlot.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entry equipment slot";
	}

}
