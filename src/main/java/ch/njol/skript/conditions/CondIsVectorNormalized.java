package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.joml.Vector3d;

@Name("Is Normalized")
@Description("Checks whether a vector is normalized i.e. length of 1")
@Examples("vector of player's location is normalized")
@Since("2.5.1")
public class CondIsVectorNormalized extends PropertyCondition<Vector3d> {
	
	static {
		register(CondIsVectorNormalized.class, "normalized", "vectors");
	}
	
	@Override
	public boolean check(Vector3d vector) {
		return Math.abs(vector.lengthSquared() - 1) < Skript.EPSILON;
	}
	
	@Override
	protected String getPropertyName() {
		return "normalized";
	}
	
}
