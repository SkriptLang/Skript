package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.util.Vector;

@Name("Is Normalized")
@Description("Checks whether a vector is normalized i.e. length of 1")
@Examples("vector of player's location is normalized")
@Since("2.5.1")
public class CondIsVectorNormalized extends PropertyCondition<Vector> {
	
	static {
		register(CondIsVectorNormalized.class, "normalized", "vectors");
	}
	
	@Override
	public boolean check(Vector vector) {
		return vector.isNormalized();
	}
	
	@Override
	protected String getPropertyName() {
		return "normalized";
	}
	
}
