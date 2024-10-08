package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.FishHook;

@Name("Is Fish Hook in Open Water")
@Description("Checks whether the fish hook is in open water.")
@Examples({
	"on fish:",
		"\tif fish hook is in open water:",
			"\t\tsend \"You will catch a shark soon!\""
})
@Events("Fishing")
@Since("INSERT VERSION")
public class CondIsInOpenWater extends PropertyCondition<FishHook> {
	
	static {
		register(CondIsInOpenWater.class, PropertyType.BE, "in open water[s]", "fishinghooks");
	}

	@Override
	public boolean check(FishHook fishHook) {
		return fishHook.isInOpenWater();
	}

	@Override
	protected String getPropertyName() {
		return "in open water";
	}
	
}
