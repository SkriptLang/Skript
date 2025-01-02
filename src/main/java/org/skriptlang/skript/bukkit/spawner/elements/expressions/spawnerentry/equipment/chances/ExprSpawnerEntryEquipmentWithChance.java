package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry.equipment.chances;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipmentWrapper.DropChance;

public class ExprSpawnerEntryEquipmentWithChance extends SimplePropertyExpression<DropChance, Float> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntryEquipmentWithChance.class, Float.class,
			"equipment drop chance", "equipmentdropchances");
	}

	@Override
	public @Nullable Float convert(DropChance chance) {
		return chance.getDropChance();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Float.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		float chance = delta != null ? ((float) delta[0]) : 0;

		for (DropChance dropChance : getExpr().getArray(event)) {
			switch (mode) {
				case SET -> dropChance.setDropChance(chance);
				case ADD -> dropChance.setDropChance(dropChance.getDropChance() + chance);
				case REMOVE -> dropChance.setDropChance(dropChance.getDropChance() - chance);
				case RESET -> dropChance.setDropChance(1); // default value
			}
		}
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entry drop chance";
	}

}
