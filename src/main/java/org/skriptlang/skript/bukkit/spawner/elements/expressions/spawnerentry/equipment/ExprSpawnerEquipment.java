package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry.equipment;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipmentWrapper;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipmentWrapper.DropChance;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.List;

public class ExprSpawnerEquipment extends SimpleExpression<SpawnerEntryEquipmentWrapper> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprSpawnerEquipment.class, SpawnerEntryEquipmentWrapper.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(ExprSpawnerEquipment::new)
			.priority(SyntaxInfo.COMBINED)
			.addPattern("%loottable% with drop chance[s] %equipmentdropchances%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	private Expression<LootTable> lootTable;
	private Expression<DropChance> chances;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		lootTable = (Expression<LootTable>) exprs[0];
		chances = (Expression<DropChance>) exprs[1];
		return true;
	}

	@Override
	protected SpawnerEntryEquipmentWrapper @Nullable [] get(Event event) {
		LootTable lootTable = this.lootTable.getSingle(event);
		if (lootTable == null)
			return new SpawnerEntryEquipmentWrapper[0];

		List<DropChance> chances = Arrays.asList(this.chances.getArray(event));

		return new SpawnerEntryEquipmentWrapper[]{new SpawnerEntryEquipmentWrapper(lootTable, chances)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SpawnerEntryEquipmentWrapper> getReturnType() {
		return SpawnerEntryEquipmentWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		return builder.append("spawner equipment with", lootTable, "and", chances).toString();
	}

}
