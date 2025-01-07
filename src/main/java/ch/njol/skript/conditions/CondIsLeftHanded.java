package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.MainHand;

@Name("Left Handed")
@Description({
	"Checks if living entities or players are left or right-handed. Armor stands are neither right nor left-handed.",
	"Paper 1.17.1+ is required for non-player entities."
})
@Examples({
	"on damage of player:",
		"\tif victim is left handed:",
			"\t\tcancel event"
})
@Since("2.8.0")
@RequiredPlugins("Paper 1.17.1+ (entities)")
public class CondIsLeftHanded extends PropertyCondition<LivingEntity> {

	private static final boolean CAN_USE_ENTITIES = Skript.methodExists(Mob.class, "isLeftHanded");

	static {
		if (CAN_USE_ENTITIES) {
			register(CondIsLeftHanded.class, PropertyType.BE, "(:left|right)( |-)handed", "livingentities");
		} else {
			register(CondIsLeftHanded.class, PropertyType.BE, "(:left|right)( |-)handed", "players");
		}
	}

	private MainHand hand;

	@Override
	public boolean init(Expression[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		hand = parseResult.hasTag("left") ? MainHand.LEFT : MainHand.RIGHT;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		// check if entity is a mob and if the method exists
		if (CAN_USE_ENTITIES && livingEntity instanceof Mob)
			return ((Mob) livingEntity).isLeftHanded() == (hand == MainHand.LEFT);

		// check if entity is a player
		if (livingEntity instanceof HumanEntity)
			return ((HumanEntity) livingEntity).getMainHand() == hand;

		// invalid entity
		return false;
	}

	@Override
	protected String getPropertyName() {
		return (hand == MainHand.LEFT ? "left" : "right") + " handed";
	}
	
}
