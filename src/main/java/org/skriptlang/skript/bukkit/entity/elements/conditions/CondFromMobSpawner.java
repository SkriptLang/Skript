package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is From A Mob Spawner")
@Description("Checks if an entity was spawned from a mob spawner.")
@Example("send whether target is from a mob spawner")
@Since("2.10")
public class CondFromMobSpawner extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondFromMobSpawner.class, PropertyType.BE, "from a [mob] spawner", "livingentities")
				.addPatterns(
					"%entities% (was|were) spawned (from|by) a [mob] spawner",
					"%entities% (wasn't|weren't|was not|were not) spawned (from|by) a [mob] spawner"
				).supplier(CondFromMobSpawner::new)
				.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		//noinspection unchecked
		setExpr((Expression<Entity>) exprs[0]);
		return true;
	}

	@Override
	public boolean check(Entity entity) {
		return entity.fromMobSpawner();
	}

	@Override
	protected String getPropertyName() {
		return "from a mob spawner";
	}

}

