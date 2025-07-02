package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;

@Name("Armor Stand - Has Behaviour")
@Description("Allows users to check the behaviour of an armor stand (i.e. whether it is ticking or able to move).")
@Examples({
	"if {_armorstands::*} are not able to tick:",
	"if {_armorstand} is able to move:",
	"if {_armorstand} can tick:",
	"if {_armorstand} cannot move:"
})
@Since("INSERT VERSION")
@Keywords({"move", "moving", "ticking"})
@RequiredPlugins("Paper")
public class CondArmorStandBehaviour extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(ArmorStand.class, "canTick")) {
			register(CondArmorStandBehaviour.class, "able to (:tick|move)", "livingentities");
			register(CondArmorStandBehaviour.class, PropertyType.CAN, "(:tick|move)", "livingentities");
		}
	}

	private int pattern;
	private boolean tick;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		tick = parseResult.hasTag("tick");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		if (livingEntity instanceof ArmorStand stand)
			return tick ? stand.canTick() : stand.canMove();
		return false;
	}

	@Override
	protected PropertyType getPropertyType() {
		return pattern == 0 ? PropertyType.BE : PropertyType.CAN;
	}

	@Override
	public String getPropertyName() {
		return tick ? "able to tick" : "able to move";
	}

}
