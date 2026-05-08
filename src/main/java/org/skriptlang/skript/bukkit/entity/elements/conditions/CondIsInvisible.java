package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Invisible")
@Description("Checks whether a living entity is invisible.")
@Example("target entity is invisible")
@Since("2.7")
public class CondIsInvisible extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsInvisible.class, PropertyType.BE, "(invisible|:visible)", "livingentities")
				.supplier(CondIsInvisible::new)
				.build()
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		setNegated(matchedPattern == 1 ^ parseResult.hasTag("visible"));
		return true;
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		return livingEntity.isInvisible();
	}

	@Override
	protected String getPropertyName() {
		return "invisible";
	}

}
