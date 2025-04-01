package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Piglin;

@Name("Is Dancing")
@Description("Checks to see if an entity is dancing, such as allays, parrots, or piglins.")
@Examples({
	"if last spawned allay is dancing:",
		"\tbroadcast \"Dance Party!\""
})
@RequiredPlugins("Paper 1.20.1+ (piglins)")
@Since("INSERT VERSION")
public class CondIsDancing extends PropertyCondition<LivingEntity> {

	private static final boolean SUPPORTS_PIGLINS = Skript.methodExists(Piglin.class, "isDancing");

	static {
		register(CondIsDancing.class, "dancing", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Allay allay) {
			return allay.isDancing();
		} else if (entity instanceof Parrot parrot) {
			return parrot.isDancing();
		} else if (SUPPORTS_PIGLINS && entity instanceof Piglin piglin) {
			return piglin.isDancing();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "dancing";
	}

}
