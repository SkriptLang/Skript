package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Flammable")
@Description("Checks whether an item is flammable.")
@Examples({
	"wood is flammable",
	"player's tool is flammable"
})
@Since("2.2-dev36")
public class CondIsFlammable extends PropertyCondition<ItemType> {
	
	static {
		register(CondIsFlammable.class, "flammable", "itemtypes");
	}
	
	@Override
	public boolean check(ItemType itemType) {
		return itemType.getMaterial().isFlammable();
	}
	
	@Override
	protected String getPropertyName() {
		return "flammable";
	}
	
}
