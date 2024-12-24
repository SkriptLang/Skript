package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Can Pick Up Items")
@Description("Whether living entities are able to pick up items off the ground or not.")
@Examples({
	"if player can pick items up:",
		"\tsend \"You can pick up items!\" to player",
	"",
	"on drop:",
		"\tif player can't pick	up items:",
			"\t\tsend \"Be careful, you won't be able to pick that up!\" to player"
})
@Since("2.8.0")
public class CondCanPickUpItems extends PropertyCondition<LivingEntity> {

	static {
		register(CondCanPickUpItems.class, PropertyType.CAN, "pick([ ]up items| items up)", "livingentities");
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		return livingEntity.getCanPickupItems();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}

	@Override
	protected String getPropertyName() {
		return "pick up items";
	}

}
