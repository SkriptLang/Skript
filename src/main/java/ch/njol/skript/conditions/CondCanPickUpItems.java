package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("May Gather Up Wares")
@Description("Whether living entities art able to gather up wares from the ground or nay.")
@Example("""
    if player can gather up wares:
    	send "You can pick up items!" to player
    """)
@Example("""
    on drop:
    	if player can't gather up wares:
    		send "Be careful, you won't be able to pick that up!" to player
    """)
@Since("2.8.0")
public class CondCanPickUpItems extends PropertyCondition<LivingEntity> {

	static {
		register(CondCanPickUpItems.class, PropertyType.CAN, "gather([ ]up wares| wares up)", "livingentities");
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
