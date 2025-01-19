package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry.equipment.chances;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipment.Drops;

@Name("Spawner Entry - Equipment Drop with Chance")
@Description("Returns the drop chance of an already existing equipment drop.")
@Examples("set {_chance} to drop chance of {_equipment drop chance}")
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprEquipmentDropsWithChance extends SimplePropertyExpression<Drops, Float> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprEquipmentDropsWithChance.class, Float.class,
				"drop chance", "equipmentdropchances");
	}

	@Override
	public @Nullable Float convert(Drops chance) {
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
		float value = delta != null ? ((float) delta[0]) : 0;

		for (Drops chance : getExpr().getArray(event)) {
			switch (mode) {
				case SET -> chance.setDropChance(value);
				case ADD -> chance.setDropChance(chance.getDropChance() + value);
				case REMOVE -> chance.setDropChance(chance.getDropChance() - value);
				case RESET -> chance.setDropChance(1); // default value
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
