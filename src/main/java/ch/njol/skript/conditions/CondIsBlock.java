package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Block")
@Description("Checks whether an item is a block.")
@Example("player's held item is a block")
@Example("{list::*} are blocks")
@Since("2.4")
public class CondIsBlock extends PropertyCondition<ItemType> {
	
	static {
		register(CondIsBlock.class, "([a] block|blocks)", "itemtypes");
	}
	
	@Override
	public boolean check(ItemType itemType) {
		return itemType.getMaterial().isBlock();
	}
	
	@Override
	protected String getPropertyName() {
		return "block";
	}
	
}
