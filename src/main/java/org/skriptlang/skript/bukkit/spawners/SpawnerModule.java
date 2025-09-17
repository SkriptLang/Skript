package org.skriptlang.skript.bukkit.spawners;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.yggdrasil.Fields;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.TrialSpawnerSpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.loot.LootTable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.spawners.util.SkriptSpawnerEntry;
import org.skriptlang.skript.bukkit.spawners.util.events.MobSpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.events.SpawnRuleEvent;
import org.skriptlang.skript.bukkit.spawners.util.events.SpawnerEntryEvent;
import org.skriptlang.skript.bukkit.spawners.util.events.TrialSpawnerDataEvent;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptMobSpawnerData;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptSpawnerData;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptTrialSpawnerData;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.ClassLoader;

import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@SuppressWarnings("UnstableApiUsage")
public class SpawnerModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(SkriptSpawnerData.class, "spawnerdata")
			.user("spawner ?datas?")
			.name("Spawner Data")
			.description("""
				Represents the common data of a mob spawner or a trial spawner, including spawner entries, minimum \
				and maximum spawn delays, and more.
				""")
			.defaultExpression(new EventValueExpression<>(SkriptSpawnerData.class))
			.since("INSERT VERSION")
		);

		Classes.registerClass(new ClassInfo<>(SkriptMobSpawnerData.class, "mobspawnerdata")
			.user("mob ?spawner ?datas?")
			.name("Mob Spawner Data")
			.description("""
				Represents the mob spawner data that can be contained in a monster spawner or a spawner minecart. \
				Additional information can be found on \
				<a href='https://minecraft.wiki/w/Monster_Spawner'>the Minecraft wiki page about mob spawners</a>.
				""")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(SkriptMobSpawnerData.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SkriptMobSpawnerData data, int flags) {
					return "mob spawner data";
				}

				@Override
				public String toVariableNameString(SkriptMobSpawnerData data) {
					return "mob_spawner_data:" + data.hashCode();
				}
			})
			.serializer(new YggdrasilSerializer<>())
		);

		Classes.registerClass(new ClassInfo<>(SkriptTrialSpawnerData.class, "trialspawnerdata")
			.user("trial ?spawner ?datas?")
			.name("Trial Spawner Data")
			.description("""
				Represents the static data of a trial spawner, including details such as \
				activation range, reward entries, and more.
				Additional information can be found on \
				<a href='https://minecraft.wiki/w/Trial_Spawner'>the Minecraft wiki page about trial spawners</a>.
				""")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(SkriptTrialSpawnerData.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SkriptTrialSpawnerData data, int flags) {
					return "trial spawner data";
				}

				@Override
				public String toVariableNameString(SkriptTrialSpawnerData data) {
					return "trial_spawner_data:" + data.hashCode();
				}
			})
			.serializer(new YggdrasilSerializer<>())
		);

		Classes.registerClass(new ClassInfo<>(SkriptSpawnerEntry.class, "spawnerentry")
			.user("spawner ?entr(y|ies)")
			.name("Spawner Entry")
			.description("""
				A spawner entry represents what entity can spawn from a spawner, including details such as \
				spawn rules, spawn weight, and equipment.
				More information about spawner entries can be found on \
				<a href='https://minecraft.wiki/w/Monster_Spawner'>the Minecraft wiki page about spawners</a>.
				""")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(SkriptSpawnerEntry.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SkriptSpawnerEntry entry, int flags) {
					return "spawner entry of " + Classes.toString(entry.getEntitySnapshot());
				}

				@Override
				public String toVariableNameString(SkriptSpawnerEntry entry) {
					return "spawner_entry:" + entry.hashCode();
				}
			})
			.serializer(new Serializer<>() {
				@Override
				public Fields serialize(SkriptSpawnerEntry entry) {
					Fields fields = new Fields();

					fields.putPrimitive("weight", entry.weight());
					fields.putObject("entity_snapshot", entry.getEntitySnapshot());
					fields.putObject("spawn_rule", entry.getSpawnRule());
					fields.putObject("equipment_loot_table", entry.getEquipmentLootTable());

					int count = 0;
					for (var entrySet : entry.getDropChances().entrySet()) {
						fields.putObject("equipment_slot_" + count, entrySet.getKey());
						fields.putPrimitive("chance_" + count, entrySet.getValue());
						count++;
					}

					return fields;
				}

				@Override
				public void deserialize(SkriptSpawnerEntry entry, Fields fields) {
					assert false;
				}

				@Override
				protected SkriptSpawnerEntry deserialize(Fields fields) throws StreamCorruptedException {
					//noinspection DataFlowIssue
					SkriptSpawnerEntry entry = new SkriptSpawnerEntry(fields.getObject("entity_snapshot", EntitySnapshot.class));
					entry.setWeight(fields.getPrimitive("weight", int.class));
					entry.setSpawnRule(fields.getObject("spawn_rule", SpawnRule.class));
					entry.setEquipmentLootTable(fields.getObject("equipment_loot_table", LootTable.class));

					Map<EquipmentSlot, Float> dropChances = new HashMap<>();
					int count = 0;
					while (fields.contains("equipment_slot_" + count)) {
						EquipmentSlot slot = fields.getObject("equipment_slot_" + count, EquipmentSlot.class);
						float chance = fields.getPrimitive("chance_" + count, float.class);
						dropChances.put(slot, chance);
						count++;
					}

					entry.setDropChances(dropChances);
					return entry;
				}

				@Override
				public boolean mustSyncDeserialization() {
					return true;
				}

				@Override
				protected boolean canBeInstantiated() {
					return false;
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(SpawnRule.class, "spawnrule")
			.user("spawn ?rules?")
			.name("Spawn Rule")
			.description("""
				Spawn rules specify the light levels required for a spawner entry to spawn. \
				More information can be found on \
				<a href='https://minecraft.wiki/w/Monster_Spawner'>the Minecraft wiki page about spawners</a>.
				""")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(SpawnRule.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SpawnRule rule, int flags) {
					StringJoiner joiner = new StringJoiner(" ", "spawn rule with", "");
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
			.serializer(new Serializer<>() {
				@Override
				public Fields serialize(SpawnRule rule) {
					Fields fields = new Fields();
					fields.putPrimitive("min_block_light", rule.getMinBlockLight());
					fields.putPrimitive("max_block_light", rule.getMaxBlockLight());
					fields.putPrimitive("min_sky_light", rule.getMinSkyLight());
					fields.putPrimitive("max_sky_light", rule.getMaxSkyLight());
					return fields;
				}

				@Override
				public void deserialize(SpawnRule rule, Fields fields) {
					assert false;
				}

				@Override
				protected SpawnRule deserialize(Fields fields) throws StreamCorruptedException {
					int minBlockLight = fields.getPrimitive("min_block_light", int.class);
					int maxBlockLight = fields.getPrimitive("max_block_light", int.class);
					int minSkyLight = fields.getPrimitive("min_sky_light", int.class);
					int maxSkyLight = fields.getPrimitive("max_sky_light", int.class);
					return new SpawnRule(minBlockLight, maxBlockLight, minSkyLight, maxSkyLight);
				}

				@Override
				public boolean mustSyncDeserialization() {
					return true;
				}

				@Override
				protected boolean canBeInstantiated() {
					return false;
				}
			})
		);

		EventValues.registerEventValue(TrialSpawnerDataEvent.class, SkriptTrialSpawnerData.class, TrialSpawnerDataEvent::getSpawnerData);
		EventValues.registerEventValue(MobSpawnerDataEvent.class, SkriptMobSpawnerData.class, MobSpawnerDataEvent::getSpawnerData);
		EventValues.registerEventValue(SpawnerEntryEvent.class, SkriptSpawnerEntry.class, SpawnerEntryEvent::getSpawnerEntry);
		EventValues.registerEventValue(SpawnRuleEvent.class, SpawnRule.class, SpawnRuleEvent::getSpawnRule);

		EventValues.registerEventValue(SpawnerSpawnEvent.class, Location.class, SpawnerSpawnEvent::getLocation);
		EventValues.registerEventValue(SpawnerSpawnEvent.class, Entity.class, SpawnerSpawnEvent::getEntity);

		EventValues.registerEventValue(TrialSpawnerSpawnEvent.class, Block.class, event -> event.getTrialSpawner().getBlock());
		EventValues.registerEventValue(TrialSpawnerSpawnEvent.class, Location.class, TrialSpawnerSpawnEvent::getLocation);
		EventValues.registerEventValue(TrialSpawnerSpawnEvent.class, Entity.class, TrialSpawnerSpawnEvent::getEntity);

		EventValues.registerEventValue(PreSpawnerSpawnEvent.class, EntityData.class,
			event -> EntityUtils.toSkriptEntityData(event.getType()));
	}

	@Override
	public void load(SkriptAddon addon) {
		ClassLoader.builder()
			.basePackage("org.skriptlang.skript.bukkit.spawners.elements")
			.deep(true)
			.initialize(true)
			.forEachClass(clazz -> {
				if (SyntaxElement.class.isAssignableFrom(clazz)) {
					try {
						clazz.getMethod("register", SyntaxRegistry.class).invoke(null, addon.syntaxRegistry());
					} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
						Bukkit.getLogger().severe("Failed to load syntax class: " + e);
					}
				}
			})
			.build()
			.loadClasses(Skript.class, Skript.getAddonInstance().getFile());
	}

}
