package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;

@Name("Allay Can Duplicate")
@Description("Checks to see if an allay is able to duplicate naturally.")
@Examples({
	"if last spawned allay can duplicate:",
		"\tdisallow last spawned to duplicate"
})
@Since("INSERT VERSION")
public class CondAllayCanDuplicate extends PropertyCondition<LivingEntity> {

	static {
		register(CondAllayCanDuplicate.class, PropertyType.CAN, "(duplicate|clone)", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Allay allay ? allay.canDuplicate() : false;
	}

	@Override
	protected String getPropertyName() {
		return "duplicate";
	}

}
