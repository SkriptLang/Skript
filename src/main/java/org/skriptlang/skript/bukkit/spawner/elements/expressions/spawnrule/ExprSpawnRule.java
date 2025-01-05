package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnrule;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.block.spawner.SpawnRule;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;

@Name("Spawn Rule")
@Description("The spawn rule used in the create spawn rule section.")
@Examples("the spawn rule")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21+")
public class ExprSpawnRule extends EventValueExpression<SpawnRule> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnRule.class, SpawnRule.class, "[the] spawn rule");
	}

	public ExprSpawnRule() {
		super(SpawnRule.class);
	}

	@Override
	public String toString() {
		return "the spawn rule";
	}

}
