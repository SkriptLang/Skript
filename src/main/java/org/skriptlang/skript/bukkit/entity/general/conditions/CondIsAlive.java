package org.skriptlang.skript.bukkit.entity.general.conditions;

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

@Name("Is Alive")
@Description("Checks whether an entity is alive. Works for non-living entities too.")
@Example("if {villager-buddy::%player's uuid%} is not dead:")
@Example("""
	on shoot:
		while the projectile is alive:
	""")
@Since("2.0, 2.4-alpha4 (non-living entity support)")
public class CondIsAlive extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsAlive.class, PropertyType.BE, "(alive|1¦dead)", "entities")
				.supplier(CondIsAlive::new)
				.build()
		);
	}

	private boolean isNegated;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isNegated = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Entity entity) {
		return isNegated == entity.isDead();
	}

	@Override
	protected String getPropertyName() {
		return isNegated ? "dead" : "alive";
	}

}
