package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Since;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.Skript;
import ch.njol.util.Kleenean;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;

@Name("Is Jumping")
@Description("Checks whether a living entity is jumping. This condition does not work on players.")
@Examples({
	"on spawn of zombie:",
		"\twhile event-entity is not jumping:",
			"\t\twait 5 ticks",
		"\tpush event-entity upwards"
})
@Since("2.8.0")
@RequiredPlugins("Paper 1.15+")
public class CondIsJumping extends PropertyCondition<LivingEntity> {
	
	static {
		if (Skript.methodExists(LivingEntity.class, "isJumping"))
			register(CondIsJumping.class, "jumping", "livingentities");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (HumanEntity.class.isAssignableFrom(exprs[0].getReturnType())) {
			Skript.error("The 'is jumping' condition only works on mobs.");
			return false;
		}
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		return livingEntity.isJumping();
	}

	@Override
	protected String getPropertyName() {
		return "jumping";
	}

}
