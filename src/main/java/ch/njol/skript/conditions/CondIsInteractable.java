package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;

@Name("Is Interactable")
@Description("Checks wether or not a block is interactable.")
@Examples({
	"on block break:",
		"\tif event-block is interactable:",
			"\t\tcancel event",
			"\t\tsend \"You cannot break interactable blocks!\""
})
@Since("2.5.2")
public class CondIsInteractable extends PropertyCondition<ItemType> {
	
	static {
		register(CondIsInteractable.class, "interactable", "itemtypes");
	}
	
	@Override
	public boolean check(ItemType item) {
		return item.getMaterial().isInteractable();
	}
	
	@Override
	protected String getPropertyName() {
		return "interactable";
	}
}
