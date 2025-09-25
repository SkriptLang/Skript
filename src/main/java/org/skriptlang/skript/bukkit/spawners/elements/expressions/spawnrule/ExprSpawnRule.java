package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawnrule;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.block.spawner.SpawnRule;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawn Rule")
@Description("""
	The spawn rule used in the spawn rule section.
	""")
@Examples("""
	the spawn rule
	""")
@Since("INSERT VERSION")
public class ExprSpawnRule extends EventValueExpression<SpawnRule> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSpawnRule.class, SpawnRule.class, "[the] spawn rule")
			.supplier(ExprSpawnRule::new)
			.build()
		);
	}

	public ExprSpawnRule() {
		super(SpawnRule.class);
	}

	@Override
	public String toString() {
		return "the spawn rule";
	}

}
