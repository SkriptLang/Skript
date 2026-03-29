package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Be Occluding")
@Description("Doth ascertain whether an item be a block that doth wholly obstruct one's vision, as a wall before thine eyes.")
@Example("player's tool is occluding")
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
