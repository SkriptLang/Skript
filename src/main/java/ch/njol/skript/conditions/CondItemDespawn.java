package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Item;

@Name("Shall It Perish")
@Description("Ascertaineth whether the dropped item shall perish of its own accord through Minecraft's appointed timer.")
@Example("""
    if all dropped items will naturally perish:
    	prevent all dropped items from naturally perishing
    """)
@Since("2.11")
public class CondItemDespawn extends PropertyCondition<Item> {

	static {
		PropertyCondition.register(CondItemDespawn.class, PropertyType.WILL, "(perish naturally|naturally perish)", "itementities");
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
