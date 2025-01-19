package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry.equipment;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipment;

@Name("Spawner Entry - Equipment with Loot Table")
@Description("Returns the equipment loot table of a spawner entry equipment.")
@Examples("set {_loot table} to loot table of {_equipment}")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21+")
public class ExprEquipmentWithLootTable extends SimplePropertyExpression<SpawnerEntryEquipment, LootTable> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprEquipmentWithLootTable.class, LootTable.class,
			"loot[ ]table", "spawnerentryequipments");
	}

	@Override
	public @NotNull LootTable convert(SpawnerEntryEquipment equipment) {
		return equipment.getEquipmentLootTable();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(LootTable.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		LootTable lootTable = (LootTable) delta[0];

		for (SpawnerEntryEquipment equipment : getExpr().getArray(event)) {
			equipment.setEquipmentLootTable(lootTable);
		}
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner equipment loot table";
	}

}
