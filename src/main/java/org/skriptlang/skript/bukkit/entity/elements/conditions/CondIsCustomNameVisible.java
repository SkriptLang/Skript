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
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Custom Name Visible")
@Description("Checks if an entity's custom name is visible.")
@Example("send true if target's custom name is visible")
@Since("2.10")
public class CondIsCustomNameVisible extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			SyntaxInfo.builder(CondIsCustomNameVisible.class)
				.addPatterns(
					"%entities%'[s] custom name[s] (is|are) visible",
					"%entities%'[s] custom name[s] (isn't|is not|are not|aren't) visible",
					"custom name of %entities% (is|are) visible",
					"custom name of %entities% (isn't|is not|are not|aren't) visible"
				).supplier(CondIsCustomNameVisible::new)
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
		return entity.isCustomNameVisible();
	}

	@Override
	protected String getPropertyName() {
		return "custom name";
	}

}

