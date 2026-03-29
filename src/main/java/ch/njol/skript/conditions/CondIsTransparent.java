package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Be It Transparent")
@Description(
	"Ascertaineth whether an item doth possess transparency. Mark well that this condition may not avail for all blocks, "
		+ "for the transparency ledger employed by Spigot is not entirely faithful."
)
@Example("player's tool is of transparent nature.")
@Since("2.2-dev36")
public class CondIsTransparent extends PropertyCondition<ItemType> {
	
	static {
		register(CondIsTransparent.class, "of transparent nature", "itemtypes");
	}
	
	@Override
	public boolean check(ItemType itemType) {
		return itemType.getMaterial().isTransparent();
	}
	
	@Override
	protected String getPropertyName() {
		return "transparent";
	}
	
}
