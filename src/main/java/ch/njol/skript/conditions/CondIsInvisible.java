package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;

@Name("Is Invisible")
@Description("Checks whether a living entity is invisible.")
@Examples("target entity is invisible")
@Since("2.7")
public class CondIsInvisible extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(LivingEntity.class, "isInvisible"))
			register(CondIsInvisible.class, PropertyType.BE, "(invisible|:visible)", "livingentities");
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
