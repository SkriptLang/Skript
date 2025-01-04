package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.block.spawner.SpawnerEntry;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;

public class ExprSpawnerEntry extends EventValueExpression<SpawnerEntry> {

    static {
        register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntry.class, SpawnerEntry.class,
	        "[the] spawner entry");
    }

    public ExprSpawnerEntry() {
        super(SpawnerEntry.class);
    }

    @Override
    public String toString() {
        return "the spawner entry";
    }

}
