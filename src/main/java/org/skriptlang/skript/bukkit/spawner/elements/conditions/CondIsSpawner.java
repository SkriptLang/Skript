package org.skriptlang.skript.bukkit.spawner.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

public class CondIsSpawner extends PropertyCondition<Object> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, CondIsSpawner.class, "a [:base] spawner", "entities/blocks");
	}

	private boolean base;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		base = parseResult.hasTag("base");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Object object) {
		if (base)
			return SpawnerUtils.isBaseSpawner(object);
		return SpawnerUtils.isSpawner(object);
	}

	@Override
	protected String getPropertyName() {
		return "is a " + (base ? "base " : "") + "spawner";
	}

}
