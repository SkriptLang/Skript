package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;

@Name("Is Screaming")
@Description("Check if a goat or enderman (Paper) is or is not screaming.")
@Examples({
	"if last spawned goat is not screaming:",
		"\tmake last spawned goat scream",
	"",
	"if {_enderman} is screaming:",
		"\tforce {_enderman} to stop screaming"
})
@RequiredPlugins("Paper (enderman)")
@Since("INSERT VERSION")
public class CondIsScreaming extends PropertyCondition<LivingEntity> {

	private static final boolean SUPPORTS_ENDERMAN = Skript.methodExists(Enderman.class, "isScreaming");

	static {
		register(CondIsScreaming.class, "screaming", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Goat goat) {
			return goat.isScreaming();
		} else if (SUPPORTS_ENDERMAN && entity instanceof Enderman enderman) {
			return enderman.isScreaming();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "screaming";
	}

}
