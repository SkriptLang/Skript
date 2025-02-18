package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Transparent")
@Description("Checks whether an item is transparent. Note that this condition may not work for all blocks, due to the transparency list used by Spigot not being completely accurate.")
@Examples("player's tool is transparent.")
@Since("2.2-dev36")
public class CondIsTransparent extends PropertyCondition<ItemType> {
	
	static {
		register(CondIsTransparent.class, "transparent", "itemtypes");
	}
	
	@Override
	public boolean check(ItemType i) {
		return i.getMaterial().isTransparent();
	}
	
	@Override
	protected String getPropertyName() {
		return "transparent";
	}
	
}
