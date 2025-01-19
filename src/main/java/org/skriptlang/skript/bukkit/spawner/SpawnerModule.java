package org.skriptlang.skript.bukkit.spawner;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.common.AnyWeight;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.TrialSpawnerSpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.spawner.util.SpawnRuleWrapper;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipment;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEntryEquipment.Drops;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerConfig;
import org.skriptlang.skript.bukkit.spawner.util.WeightedLootTable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.IOException;
import java.util.StringJoiner;

public class SpawnerModule implements AddonModule {

	public static SyntaxRegistry SYNTAX_REGISTRY;

	@Override
	public void load(SkriptAddon addon) {
		if (!Skript.isRunningMinecraft(1, 21))
			return;

		Classes.registerClass(new ClassInfo<>(TrialSpawnerConfig.class, "trialspawnerconfig")
			.user("(trial ?)?spawner ?config(urations?)?")
			.name("Trial Spawner Configuration")
			.description("Represents a trial spawner configuration. Trial spawner configurations fall under the "
				+ "base spawner category, having more configuration options. When using the base spawner expressions, "
				+ "effects or conditions, you can use this configuration to specify the type of trial spawner you want. "
				+ "If you were to specify a trial spawner block in those expressions, it would use the current configuration."
				+ "You can find more information about this in the Minecraft wiki for "
				+ "<a href='https://minecraft.wiki/w/Trial_Spawner'>trial spawners</a>")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(TrialSpawnerConfig config, int flags) {
					StringBuilder builder = new StringBuilder();
					if (config.ominous()) {
						builder.append("ominous ");
					} else {
						builder.append("normal ");
					}
					builder.append("trial spawner configuration of trial spawner at ")
						.append(Classes.toString(config.state().getLocation()));
					return builder.toString();
				}

				@Override
				public String toVariableNameString(TrialSpawnerConfig config) {
					return "trial spawner configuration:" + config.hashCode();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(WeightedLootTable.class, "weightedloottable")
			.user("weighted ?loot ?tables?")
			.name("Weighted Loot Table")
			.description(
				"Represents a weighted loot table. Trial spawners pick a weighted loot table to use as its reward.",
				"Note that this is a weighted type. That is, you can use 'weight of %weighted loot table%'")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				public
				@Override String toString(WeightedLootTable reward, int flags) {
					return "trial spawner reward with "
						+ Classes.toString(reward.getLootTable())
						+ " and weight " + reward.weight();
				}

				@Override
				public String toVariableNameString(WeightedLootTable reward) {
					return "trial spawner reward:" + reward.getLootTable().getKey() + ',' + reward.weight();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(SpawnerEntry.class, "spawnerentry")
			.user("spawner ?entry")
			.name("Spawner Entry")
			.description("Represents a spawner entry. Spawner entries are entities that spawn from a spawner with "
				+ "more configuration, e.g. spawn rules, spawn weight, and equipment. You can find more information "
				+ "about this in the Minecraft wiki for <a href='https://minecraft.wiki/w/Monster_Spawner'>spawners</a>",
				"Note that this is a weighted type. That is, you can use 'weight of %spawner entry%'")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(SpawnerEntry.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SpawnerEntry entry, int flags) {
					StringBuilder builder = new StringBuilder();

					builder.append("spawner entry with ").append(Classes.toString(entry.getSnapshot()));
					if (entry.getSpawnRule() != null)
						builder.append(", ").append(Classes.toString(entry.getSpawnRule()));
					builder.append(" and weight ").append(entry.getSpawnWeight());

					return builder.toString();
				}

				@Override
				public String toVariableNameString(SpawnerEntry entry) {
					return "spawner entry:" + entry.getSnapshot() + ',' + entry.getSpawnRule() + ',' + entry.getSpawnWeight();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(SpawnerEntryEquipment.class, "spawnerentryequipment")
			.user("spawner ?entry ?equipments?")
			.name("Spawner Entry Equipment")
			.description(
				"Represents a spawner entry equipment. Spawner entry equipments are used to specify the equipment "
				+ "that an entity will spawn with. This includes the equipment loot table and the drop chances for each "
				+ "equipment slot. You can find more information about this in the Minecraft wiki for "
				+ "<a href='https://minecraft.wiki/w/Monster_Spawner'>spawners</a>")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SpawnerEntryEquipment equipment, int flags) {
					return "spawner entry equipment with "
						+ Classes.toString(equipment.getEquipmentLootTable())
						+ " and "
						+ Classes.toString(equipment.getDropChances().toArray(), true);
				}

				@Override
				public String toVariableNameString(SpawnerEntryEquipment equipment) {
					return "spawner entry equipment:" + equipment.hashCode();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(Drops.class, "equipmentdropchance")
			.user("(spawner entry ?)?equipment ?drop ?chances?")
			.name("Spawner Entry Equipment Drop Chance")
			.description("Represents a spawner entry's equipment drop chance. This is used to specify the drop chance "
				+ "for an equipment slot. You can find more information about this in the Minecraft wiki for "
				+ "<a href='https://minecraft.wiki/w/Monster_Spawner'>spawners</a>")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(Drops equipment, int flags) {
					return "equipment drop with chance "
						+ equipment.getDropChance()
						+ " for "
						+ Classes.toString(equipment.getEquipmentSlot());
				}

				@Override
				public String toVariableNameString(Drops equipment) {
					return "equipment drop:" + equipment.getEquipmentSlot() + ',' + equipment.getDropChance();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(SpawnRule.class, "spawnrule")
			.user("spawn ?rules?")
			.name("Spawn Rule")
			.description("Represents a spawn rule. Spawn rules are used to specify the light levels required for an entity "
				+ "to spawn. You can find more information about this in the Minecraft wiki for "
				+ "<a href='https://minecraft.wiki/w/Monster_Spawner'>spawners</a>")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(SpawnRuleWrapper.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SpawnRule rule, int flags) {
					StringJoiner joiner = new StringJoiner(" ");
					joiner.add("spawn rule with");
					joiner.add("min block light " + rule.getMinBlockLight() + ',');
					joiner.add("max block light " + rule.getMaxBlockLight() + ',');
					joiner.add("min sky light " + rule.getMinSkyLight() + ", and");
					joiner.add("max sky light " + rule.getMaxSkyLight());
					return joiner.toString();
				}

				@Override
				public String toVariableNameString(SpawnRule rule) {
					return "spawn rule:"
							+ rule.getMinBlockLight() + ','
							+ rule.getMaxBlockLight() + ','
							+ rule.getMinSkyLight() + ','
							+ rule.getMaxSkyLight();
				}
			})
		);

		//todo: remove after merge of equippable pr
		Classes.registerClass(new EnumClassInfo<>(EquipmentSlot.class, "equipmentslot", "equipment slots")
			.user("equipment slot")
			.name("Equipment Slot")
			.description("Represents an equipment slot.")
			.since("INSERT VERSION")
		);

		SYNTAX_REGISTRY = addon.syntaxRegistry();
		try {
			Skript.getAddonInstance().loadClasses(
				"org.skriptlang.skript.bukkit.spawner",
				"elements");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Converters.registerConverter(SpawnerEntry.class, AnyWeight.class,
			entry -> new AnyWeight() {
				@Override
				public @NotNull Integer weight() {
					return entry.getSpawnWeight();
				}

				@Override
				public boolean supportsWeightChange() {
					return true;
				}

				@Override
				public void setWeight(Integer weight) throws UnsupportedOperationException {
					if (weight > 0)
						entry.setSpawnWeight(weight);
				}
		}, Converter.NO_RIGHT_CHAINING);

		EventValues.registerEventValue(SpawnerSpawnEvent.class, Block.class, event -> {
			if (event.getSpawner() != null)
				return event.getSpawner().getBlock();
			return null;
		});
		EventValues.registerEventValue(SpawnerSpawnEvent.class, Location.class, SpawnerSpawnEvent::getLocation);
		EventValues.registerEventValue(SpawnerSpawnEvent.class, Entity.class, SpawnerSpawnEvent::getEntity);

		EventValues.registerEventValue(TrialSpawnerSpawnEvent.class, Block.class, event -> event.getTrialSpawner().getBlock());
		EventValues.registerEventValue(TrialSpawnerSpawnEvent.class, Location.class, TrialSpawnerSpawnEvent::getLocation);
		EventValues.registerEventValue(TrialSpawnerSpawnEvent.class, Entity.class, TrialSpawnerSpawnEvent::getEntity);

		if (Skript.classExists("com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent")) {
			EventValues.registerEventValue(PreSpawnerSpawnEvent.class, EntityData.class,
				event -> EntityUtils.toSkriptEntityData(event.getType()));
		}
	}

}
