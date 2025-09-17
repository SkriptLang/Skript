package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SkriptSpawnerEntry;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawner Entry Equipment")
@Description("""
    Returns the equipment loot table of a spawner entry. This loot table determines the equipment \
    (armor, weapons, tools, etc.) that the spawned entity will have.
    Only loot tables specifically defined as equipment loot tables will have effect.
    """)
@Example("""
	set {_entry} to the spawner entry of a pig:
		set the spawner entry equipment to loot table "minecraft:equipment/trial_chamber"
		set the drop chances for helmet and boots to 100%
	""")
public class ExprSpawnerEntryEquipment extends SimplePropertyExpression<SkriptSpawnerEntry, LootTable> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSpawnerEntryEquipment.class, LootTable.class,
			"spawner entry equipment[s]", "spawnerentries", true)
				.supplier(ExprSpawnerEntryEquipment::new)
				.build()
		);
	}

	@Override
	public @Nullable LootTable convert(SkriptSpawnerEntry entry) {
		return entry.getEquipmentLootTable();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(LootTable.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		LootTable lootTable = delta != null ? (LootTable) delta[0] : null;
		for (SkriptSpawnerEntry entry : getExpr().getArray(event)) {
			entry.setEquipmentLootTable(lootTable);
		}
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entry equipment";
	}

}
