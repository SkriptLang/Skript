package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;

@Name("Is In Love")
@Description("Whether or not the animals are currently in the love state.")
@Examples({
	"on spawn of living entity:",
		"\tif entity is in love:",
			""
})
@Since("INSERT VERSION")
public class CondIsInLove extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsInLove.class, "in lov(e|ing) [state]", "livingentities");
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		if (livingEntity instanceof Animals)
			return ((Animals) livingEntity).isLoveMode();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "in love";
	}

}
