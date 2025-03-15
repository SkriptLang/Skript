package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffGoatHorns.GoatHorn;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;

@Name("Goat Has Horns")
@Description("Checks to see if a goat has or does not have a left, right, or both horns.")
@Examples({
	"if last spawned goat does not have both horns:",
		"\tmake last spawned goat have both horns",
	"",
	"if {_goat} has a right horn:",
		"\tforce {_goat} to not have a right horn"
})
@Since("INSERT VERSION")
public class CondGoatHasHorns extends PropertyCondition<LivingEntity> {

	static {
		register(CondGoatHasHorns.class, PropertyType.HAVE,
			"((any|a) horn|left:[a] left horn[s]|right:[a] right horn[s]|both:both horns)", "livingentities");
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
