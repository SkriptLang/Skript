package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Edible")
@Description("Checks whether an item is edible.")
@Examples({"steak is edible", "player's tool is edible"})
@Since("2.2-dev36")
public class CondIsEdible extends PropertyCondition<ItemType> {

	static {
		PropertyCondition.register(CondIsEdible.class, "edible", "itemtypes");
	}

	@Override
	public boolean check(ItemType i) {
		return i.getMaterial().isEdible();
	}

	@Override
	protected String getPropertyName() {
		return "edible";
	}

}
