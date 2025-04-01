package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Item;

@Name("Will Despawn")
@Description("Checks if the dropped item will be despawned naturally through Minecraft's timer.")
@Examples({
	"if all dropped items can despawn naturally:",
		"\tprevent all dropped items from naturally despawning"
})
@Since("INSERT VERSION")
public class CondItemDespawn extends PropertyCondition<Item> {

	static {
		PropertyCondition.register(CondItemDespawn.class, PropertyType.WILL, "(despawn naturally|naturally despawn)", "itementities");
		PropertyCondition.register(CondItemDespawn.class, PropertyType.CAN, "(despawn naturally|naturally despawn)", "itementities");
	}

	@Override
	public boolean check(Item item) {
		return !item.isUnlimitedLifetime();
	}

	@Override
	protected String getPropertyName() {
		return "naturally despawn";
	}

}
