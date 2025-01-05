package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipmentWrapper;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipmentWrapper.Drops;

import java.util.ArrayList;
import java.util.List;

@Name("Spawner Entry - Equipment")
@Description("The equipment of the spawner entry. This determines what equipment the entity will wear once spawned.")
@Examples({
	"set {_entry} to a spawner entry using entity snapshot of a skeleton:",
		"\tset {_dropchances::*} to helmet slot with drop chance 50%, chestplate slot with drop chance 25%",
		"\tset equipment loot table to loot table \"minecraft:equipment/trial_chamber\" with {_dropchances::*}",
	"set spawner entity of event-block to {_entry}"
})
@Since("INSERT VERSION")
public class ExprSpawnerEntryWithEquipment extends SimplePropertyExpression<SpawnerEntry, SpawnerEntryEquipmentWrapper> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntryWithEquipment.class, SpawnerEntryEquipmentWrapper.class,
			"equipment loot[ ]table", "spawnerentries");
	}

	@Override
	public @Nullable SpawnerEntryEquipmentWrapper convert(SpawnerEntry entry) {
		if (entry.getEquipment() != null) {
			List<Drops> equipment = new ArrayList<>();
			entry.getEquipment().getDropChances().forEach(
				(slot, chance) -> equipment.add(new Drops(slot, chance))
			);
			return new SpawnerEntryEquipmentWrapper(entry.getEquipment().getEquipmentLootTable(), equipment);
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(SpawnerEntryEquipmentWrapper.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		SpawnerEntryEquipmentWrapper equipment = (SpawnerEntryEquipmentWrapper) delta[0];

		for (SpawnerEntry entry : getExpr().getArray(event)) {
			entry.setEquipment(equipment.getEquipment());
		}
	}

	@Override
	public Class<? extends SpawnerEntryEquipmentWrapper> getReturnType() {
		return SpawnerEntryEquipmentWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entry equipment";
	}

}
