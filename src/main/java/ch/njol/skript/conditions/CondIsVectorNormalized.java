package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import org.bukkit.util.Vector;

@Name("Be It Normalized")
@Description("Ascertaineth whether a vector be normalized, that is to say, of a length equal to one.")
@Example("vector of player's location is normalized")
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
	public Condition simplify() {
		if (getExpr() instanceof Literal<? extends Vector>)
			return SimplifiedCondition.fromCondition(this);
		return this;
	}

	@Override
	protected String getPropertyName() {
		return "normalized";
	}
	
}
