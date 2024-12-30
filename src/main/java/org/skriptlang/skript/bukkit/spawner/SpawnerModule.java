package org.skriptlang.skript.bukkit.spawner;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.TrialSpawnerSpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.spawner.util.SpawnRuleWrapper;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEquipmentWrapper;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerEquipmentWrapper.DropChance;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerConfig;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerReward;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.IOException;
import java.util.StringJoiner;

public class SpawnerModule implements AddonModule {

	public static SyntaxRegistry SYNTAX_REGISTRY;

	@Override
	public void load(SkriptAddon addon) {
		if (!Skript.classExists("org.bukkit.spawner.BaseSpawner"))
			return;

		Classes.registerClass(new ClassInfo<>(TrialSpawnerConfig.class, "trialspawnerconfig")
			.user("(trial ?)?spawner ?config(urations?)?")
			.name("Trial Spawner Configuration")
			.description("Represents a trial spawner configuration.")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(TrialSpawnerConfig config, int flags) {
					StringBuilder builder = new StringBuilder();
					if (config.isOminous())
						builder.append("ominous ");
					else
						builder.append("normal ");
					builder.append("trial spawner configuration of trial spawner at ")
						.append(Classes.toString(config.getState().getLocation()));
					return builder.toString();
				}

				@Override
				public String toVariableNameString(TrialSpawnerConfig config) {
					return "trial spawner configuration:" + config.hashCode();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(TrialSpawnerReward.class, "trialspawnerreward")
			.user("trial ?spawner ?rewards?")
			.name("Trial Spawner Reward")
			.description("Represents a trial spawner reward.")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				public
				@Override String toString(TrialSpawnerReward reward, int flags) {
					return "trial spawner reward with "
						+ Classes.toString(reward.getLootTable())
						+ " and weight " + reward.getWeight();
				}

				@Override
				public String toVariableNameString(TrialSpawnerReward reward) {
					return "trial spawner reward:" + reward.getLootTable().getKey() + ',' + reward.getWeight();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(SpawnerEntry.class, "spawnerentry")
			.user("spawner ?entry")
			.name("Spawner Entry")
			.description("Represents a spawner entry.")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(SpawnerEntry.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SpawnerEntry entry, int flags) {
					return "spawner entry with " +
						Classes.toString(entry.getSnapshot()) +
						" and " +
						Classes.toString(entry.getSpawnRule()) +
						" and weight " +
						entry.getSpawnWeight();
				}

				@Override
				public String toVariableNameString(SpawnerEntry entry) {
					return "spawner entry:" + entry.getSnapshot() + ',' + entry.getSpawnRule() + ',' + entry.getSpawnWeight();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(SpawnerEquipmentWrapper.class, "spawnerentryequipment")
			.user("spawner ?entry ?equipments?")
			.name("Spawner Entry Equipment")
			.description("Represents a spawner entry equipment.")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SpawnerEquipmentWrapper equipment, int flags) {
					return "spawner entry equipment with " +
						Classes.toString(equipment.getEquipmentLootTable()) +
						" and chance "
						+ Classes.toString(equipment.getDropChances());
				}

				@Override
				public String toVariableNameString(SpawnerEquipmentWrapper equipment) {
					return "spawner entry equipment:" + equipment.hashCode();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(DropChance.class, "spawnerentrydropchance")
			.user("spawner ?entry ?drop ?chance")
			.name("Spawner Entry Drop Chance")
			.description("Represents a spawner entry drop chance.")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(DropChance equipment, int flags) {
					return "spawner entry drop chance with " +
						Classes.toString(equipment.getEquipmentSlot()) +
						" and chance " +
						equipment.getDropChance();
				}

				@Override
				public String toVariableNameString(DropChance equipment) {
					return "spawner entry drop chance:" + equipment.getEquipmentSlot() + ',' + equipment.getDropChance();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(SpawnRule.class, "spawnrule")
			.user("spawn ?rules?")
			.name("Spawn Rule")
			.description("Represents a spawn rule.")
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

		Classes.registerClass(new EnumClassInfo<>(TrialSpawner.State.class, "trialspawnerstate", "trial spawner states")
			.user("trial ?spawner ?state")
			.name("Trial Spawner State")
			.description("Represents a trial spawner state.")
			.since("INSERT VERSION")
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
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.spawner", "elements");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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
			EventValues.registerEventValue(PreSpawnerSpawnEvent.class, Location.class, PreSpawnerSpawnEvent::getSpawnerLocation);
			EventValues.registerEventValue(PreSpawnerSpawnEvent.class, Location.class, PreSpawnerSpawnEvent::getSpawnLocation);
		}
	}

}
