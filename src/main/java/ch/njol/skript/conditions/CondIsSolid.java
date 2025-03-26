package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Solid")
@Description("Checks whether an item is solid.")
@Examples({
	"grass block is solid",
	"player's tool isn't solid"
})
@Since("2.2-dev36")
public class CondIsSolid extends PropertyCondition<ItemType> {
	
	static {
		register(CondIsSolid.class, "solid", "itemtypes");
	}
	
	@Override
	public boolean check(ItemType itemType) {
		return itemType.getMaterial().isSolid();
	}
	
	@Override
	protected String getPropertyName() {
		return "solid";
	}
	
}
