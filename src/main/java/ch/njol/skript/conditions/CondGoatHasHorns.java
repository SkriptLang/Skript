package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.effects.EffGoatHorns.GoatHorn;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;

@Name("Goat Hath Horns")
@Description("Examineth whether a goat doth possess or doth lack a sinister, dexter, or both horns.")
@Example("""
		if last spawned goat does not have both horns:
			make last spawned goat have both horns
	"""
)
@Example("""
    if {_goat} has a dexter horn:
    	force {_goat} to not have a dexter horn
    	
    """
)
@Since("2.11")
public class CondGoatHasHorns extends PropertyCondition<LivingEntity> {

	static {
		register(CondGoatHasHorns.class, PropertyType.HAVE,
			"((any|a) horn|left:[a] sinister horn[s]|right:[a] dexter horn[s]|both:both horns)", "livingentities");
	}

	private GoatHorn goatHorn = GoatHorn.ANY;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (parseResult.hasTag("left")) {
			goatHorn = GoatHorn.LEFT;
		} else if (parseResult.hasTag("right")) {
			goatHorn = GoatHorn.RIGHT;
		} else if (parseResult.hasTag("both")) {
			goatHorn = GoatHorn.BOTH;
		}
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (!(entity instanceof Goat goat))
			return false;
		boolean leftHorn = goat.hasLeftHorn();
		boolean rightHorn = goat.hasRightHorn();
		return switch (goatHorn) {
			case ANY -> leftHorn || rightHorn;
			case BOTH -> leftHorn && rightHorn;
			case LEFT -> leftHorn;
			case RIGHT -> rightHorn;
		};
	}

	@Override
	protected String getPropertyName() {
		return switch (goatHorn) {
			case ANY -> "a horn";
			case BOTH -> "both horns";
			case LEFT -> "left horn";
			case RIGHT -> "right horn";
		};
	}

}
