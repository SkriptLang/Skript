package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SkriptSpawnerEntry;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawner Entry Rule")
@Description("""
	Returns the spawn rule of the spawner entry. Spawn rules determine the conditions under which the entry will be \
	spawned.
	""")
@Example("""
	set {_entry} to a spawner entry of a cow:
		set the spawn rule to a spawn rule:
			set the maximum block light spawn level to 15
			set the minimum block light spawn level to 7
			set the maximum sky light spawn level to 11
			set the minimum sky light spawn level to 0
		delete the spawn rule
	""")
@Since("INSERT VERSION")
public class ExprSpawnerEntryRule extends SimplePropertyExpression<SkriptSpawnerEntry, SpawnRule> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSpawnerEntryRule.class, SpawnRule.class,
			"spawn rule[s]", "spawnerentries", true)
				.supplier(ExprSpawnerEntryRule::new)
				.build()
		);
	}

	@Override
	public @Nullable SpawnRule convert(SkriptSpawnerEntry entry) {
		return entry.getSpawnRule();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(SpawnRule.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		SpawnRule rule = delta != null ? (SpawnRule) delta[0] : null;
		for (SkriptSpawnerEntry entry : getExpr().getArray(event)) {
			entry.setSpawnRule(rule);
		}
	}

	@Override
	public Class<? extends SpawnRule> getReturnType() {
		return SpawnRule.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entry rule";
	}

}
