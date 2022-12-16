package ch.njol.skript.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;



@Name("is Stackable")
@Description("Check if item is stackable.")
@Examples("%item% is stackable")
@Since("2.3")

public class CondIsStackable extends PropertyCondition<ItemType> {
    static {
        
        register(CondIsStackable.class, "stackable", "itemtypes");
    }

    @Override
    public boolean check(ItemType item) {
        return item.getMaterial().getMaxStackSize() > 1;
    }

    @Override
    protected String getPropertyName() { 
        return "stackable";
    }
}




