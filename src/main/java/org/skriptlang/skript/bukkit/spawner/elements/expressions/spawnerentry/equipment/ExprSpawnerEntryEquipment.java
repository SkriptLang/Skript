package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry.equipment;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
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
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipmentWrapper.Drops;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.List;

@Name("Spawner Entry Equipment")
@Description({
	"Returns an equipment loot table with the given drop chances. " +
		"The loot table must be an equipment loot table, otherwise the entities will spawn naked."
})
@Examples({
	"set {_entry} to a spawner entry using entity snapshot of a skeleton:",
		"\tset {_dropchances::*} to helmet slot with drop chance 50%, chestplate slot with drop chance 25%",
		"\tset equipment loot table to loot table \"minecraft:equipment/trial_chamber\" with {_dropchances::*}",
	"set spawner entity of event-block to {_entry}",
})
@Since("INSERT VERSION")
public class ExprSpawnerEntryEquipment extends SimpleExpression<SpawnerEntryEquipmentWrapper> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprSpawnerEntryEquipment.class, SpawnerEntryEquipmentWrapper.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(ExprSpawnerEntryEquipment::new)
			.priority(SyntaxInfo.COMBINED)
			.addPattern("%loottable% with [drop chance[s]] %equipmentdropchances%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	private Expression<LootTable> lootTable;
	private Expression<Drops> chances;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		lootTable = (Expression<LootTable>) exprs[0];
		chances = (Expression<Drops>) exprs[1];
		return true;
	}

	@Override
	protected SpawnerEntryEquipmentWrapper @Nullable [] get(Event event) {
		LootTable lootTable = this.lootTable.getSingle(event);
		if (lootTable == null)
			return new SpawnerEntryEquipmentWrapper[0];

		List<Drops> chances = Arrays.asList(this.chances.getArray(event));

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
