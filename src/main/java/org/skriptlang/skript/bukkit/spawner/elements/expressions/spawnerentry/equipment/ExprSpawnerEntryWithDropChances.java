package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry.equipment;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipmentWrapper;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipmentWrapper.DropChance;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

public class ExprSpawnerEntryWithDropChances extends PropertyExpression<SpawnerEntryEquipmentWrapper, DropChance> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprSpawnerEntryWithDropChances.class, DropChance.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(ExprSpawnerEntryWithDropChances::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns(
				"[the] drop chance[s] (from|of) %spawnerentryequipments%",
				"%spawnerentryequipments%'[s] drop chance[s]")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends SpawnerEntryEquipmentWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected DropChance[] get(Event event, SpawnerEntryEquipmentWrapper[] source) {
		return new DropChance[0];
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD -> CollectionUtils.array(DropChance[].class, EquipmentSlot[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;

		for (var entry : getExpr().getArray(event)) {
			if (mode == ChangeMode.SET)
				entry.setDropChances(new ArrayList<>());

			for (var object : delta) {
				if (object instanceof DropChance chance) {
					switch (mode) {
						case SET, ADD -> entry.addDropChance(chance);
						case REMOVE -> entry.removeDropChance(chance);
					}
				} else if (object instanceof EquipmentSlot slot) {
					switch (mode) {
						case SET, ADD -> entry.addDropChance(new DropChance(slot, 1));
						case REMOVE -> entry.removeDropChance(new DropChance(slot, 1));
					}
				}
			}
		}
	}

	@Override
	public Class<? extends DropChance> getReturnType() {
		return DropChance.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spawner entry drop chances of " + getExpr().toString(event, debug);
	}

}
