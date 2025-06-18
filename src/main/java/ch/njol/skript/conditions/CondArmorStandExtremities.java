package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;

@Name("Armor Stand - Has Extremities")
@Description("Allows users to check the extremities of an armor stand (i.e. whether it has arms or a base plate).")
@Examples({
	"send true if {_armorstand} has arms",
	"if {_armorstand} has no base plate:"
})
@Since("INSERT VERSION")
@Keywords({"arms", "base plate"})
public class CondArmorStandExtremities extends PropertyCondition<LivingEntity> {

	static {
		register(CondArmorStandExtremities.class, PropertyType.HAVE, "(:arms|[a] base[ ]plate)", "livingentities");
	}

	private boolean arms;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		arms = parseResult.hasTag("arms");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		if (livingEntity instanceof ArmorStand stand)
			return arms ? stand.hasArms() : stand.hasBasePlate();
		return false;
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	public String getPropertyName() {
		return arms ? "arms" : "a base plate";
	}

}
