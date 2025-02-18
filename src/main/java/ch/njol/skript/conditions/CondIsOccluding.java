package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Occluding")
@Description("Checks whether an item is a block and completely blocks vision.")
@Examples("player's tool is occluding")
@Since("2.5.1")
public class CondIsOccluding extends PropertyCondition<ItemType> {
	
	static {
		register(CondIsOccluding.class, "occluding", "itemtypes");
	}
	
	@Override
	public boolean check(ItemType item) {
		return item.getMaterial().isOccluding();
	}
	
	@Override
	protected String getPropertyName() {
		return "occluding";
	}
	
}
