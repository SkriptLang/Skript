package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEquipmentWrapper;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEquipmentWrapper.DropChance;

import java.util.ArrayList;
import java.util.List;

public class ExprSpawnerEntryWithEquipment extends SimplePropertyExpression<SpawnerEntry, SpawnerEquipmentWrapper> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntryWithEquipment.class, SpawnerEquipmentWrapper.class,
			"spawner entry [[spawned] entity] equipment", "spawnerentries");
	}

	@Override
	public @Nullable SpawnerEquipmentWrapper convert(SpawnerEntry entry) {
		if (entry.getEquipment() != null) {
			List<DropChance> equipment = new ArrayList<>();
			entry.getEquipment().getDropChances().forEach((slot, chance) -> equipment.add(new DropChance(slot, chance)));
			return new SpawnerEquipmentWrapper(entry.getEquipment().getEquipmentLootTable(), equipment);
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(SpawnerEquipmentWrapper.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		SpawnerEquipmentWrapper equipment = (SpawnerEquipmentWrapper) delta[0];

		for (SpawnerEntry entry : getExpr().getArray(event)) {
			entry.setEquipment(equipment.getEquipment());
		}
	}

	@Override
	public Class<? extends SpawnerEquipmentWrapper> getReturnType() {
		return SpawnerEquipmentWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entry equipment";
	}

}
