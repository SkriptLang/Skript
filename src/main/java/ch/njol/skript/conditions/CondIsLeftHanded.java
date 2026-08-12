package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
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
	"Checks if living entities or players are left or right-handed. Armor stands are neither right nor left-handed."
})
@Example("""
	on damage of player:
		if victim is left handed:
			cancel event
	""")
@Since("2.8.0")
public class CondIsLeftHanded extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsLeftHanded.class, "(:left|right)( |-)handed", "livingentities");
	}

	private MainHand hand;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		hand = parseResult.hasTag("left") ? MainHand.LEFT : MainHand.RIGHT;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		// check if entity is a mob and if the method exists
		if (livingEntity instanceof Mob mob)
			return mob.isLeftHanded() == (hand == MainHand.LEFT);

		// check if entity is a player
		if (livingEntity instanceof HumanEntity humanEntity)
			return humanEntity.getMainHand() == hand;

		// invalid entity
		return false;
	}

	@Override
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(LivingEntity livingEntity, boolean value, ChangeMode mode) {
		value = value == (hand == MainHand.LEFT);
		if (livingEntity instanceof Mob mob) {
			mob.setLeftHanded(value);
		} else if (livingEntity instanceof HumanEntity ignored) {
			error("It is not possible to change the main hand of a player.");
		}
	}

	@Override
	protected String getPropertyName() {
		return (hand == MainHand.LEFT ? "left" : "right") + " handed";
	}
	
}
