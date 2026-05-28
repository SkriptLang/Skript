package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import org.bukkit.Material;

@Name("Is Fuel")
@Description("Checks whether an item can be used as fuel in a furnace.")
@Example("""
	on right click on furnace:
		if player's tool is not fuel:
			send "Please hold a valid fuel item in your hand"
			cancel event
	""")
@Since("2.5.1")
public class CondIsFuel extends PropertyCondition<ItemType> {
	
	static {
		if (Skript.methodExists(Material.class, "isFuel")) {
			register(CondIsFuel.class, "[furnace] fuel", "itemtypes");
		}
	}
	
	@Override
	public boolean check(ItemType item) {
		return item.getMaterial().isFuel();
	}

	@Override
	public Condition simplify() {
		if (getExpr() instanceof Literal<? extends ItemType>)
			return SimplifiedCondition.fromCondition(this);
		return this;
	}

	@Override
	protected String getPropertyName() {
		return "fuel";
	}
	
}
