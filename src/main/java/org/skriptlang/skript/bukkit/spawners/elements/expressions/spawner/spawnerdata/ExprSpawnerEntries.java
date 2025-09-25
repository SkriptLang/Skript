package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.spawnerdata;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SkriptSpawnerEntry;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptSpawnerData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Name("Spawner Entries")
@Description("""
	Returns the spawner entries of the spawner data. \
	On each spawn attempt, the spawner selects a random entry from the list (typically the highest weighted one) \
	and spawns it. The spawner's type and entity snapshot are then overwritten with the chosen entry.
	""")
@Example("""
	set {_data} to spawner data of event-block
	set {_entries::*} to spawner entries of {_data}
	delete the spawner entries of {_data}
	""")
@Example("""
	modify the spawner data of event-block:
		set {_entry} to the spawner entry of a zombie:
			set the weight to 2
			set the spawner entry equipment to loot table "minecraft:equipment/trial_chamber"
			set the drop chances for helmet, legs and boots to 100%
			set the spawn rule to a spawn rule:
				set the minimum block light spawn level to 10
				set the maximum block light spawn level to 12
				set the maximum sky light spawn level to 5
		add {_entry} to the spawner entries
	""")
@Since("INSERT VERSION")
public class ExprSpawnerEntries extends PropertyExpression<SkriptSpawnerData, SkriptSpawnerEntry> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSpawnerEntries.class, SkriptSpawnerEntry.class)
			.supplier(ExprSpawnerEntries::new)
			.priority(DEFAULT_PRIORITY)
			.addPatterns(
				"[the] spawner entr(y|ies) [of %spawnerdatas%]",
				"[%spawnerdatas%'[s]] spawner entr(y|ies)")
			.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends SkriptSpawnerData>) exprs[0]);
		return true;
	}

	@Override
	protected SkriptSpawnerEntry @Nullable [] get(Event event, SkriptSpawnerData[] source) {
		List<SkriptSpawnerEntry> entries = new ArrayList<>();

		for (SkriptSpawnerData data : source) {
			entries.addAll(data.getSpawnerEntries());
		}

		return entries.toArray(SkriptSpawnerEntry[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;

		return CollectionUtils.array(SkriptSpawnerEntry[].class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Set<SkriptSpawnerEntry> entries = new HashSet<>();
		if (delta != null) {
			for (Object object : delta)
				entries.add((SkriptSpawnerEntry) object);
		}

		for (SkriptSpawnerData data : getExpr().getArray(event)) {
			switch (mode) {
				case SET -> data.setSpawnerEntries(entries);
				case ADD -> data.addSpawnerEntries(entries);
				case REMOVE -> data.removeSpawnerEntries(entries);
				case RESET, DELETE -> data.clearSpawnerEntries();
			}
		}
	}

	@Override
	public Class<? extends SkriptSpawnerEntry> getReturnType() {
		return SkriptSpawnerEntry.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the spawner entries of " + getExpr().toString(event, debug);
	}

}
