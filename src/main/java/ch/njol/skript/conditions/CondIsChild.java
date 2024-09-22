package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;

@Name("Is Child")
@Description("Returns whether or not a living entity is a child.")
@Examples({
	"on drink:",
		"\tevent-entity is a child",
		"\tkill event-entity"
})
@Since("INSERT VERSION")
public class CondIsChild extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsChild.class, "[a] child", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Ageable ageable)
			return !ageable.isAdult();

		return false;
	}

	@Override
	protected String getPropertyName() {
		return "a child";
	}

}
