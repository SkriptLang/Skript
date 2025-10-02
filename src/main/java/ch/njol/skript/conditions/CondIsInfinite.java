package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import ch.njol.skript.util.Timespan;
import org.bukkit.potion.PotionEffect;

@Name("Is Infinite")
@Description("Checks whether potion effects or timespans are infinite.")
@Example("all of the active potion effects of the player are infinite")
@Example("if timespan argument is infinite:")
@Since("2.7")
public class CondIsInfinite extends PropertyCondition<Object> {

	static {
		register(CondIsInfinite.class, "infinite", "potioneffects/timespans");
	}

	@Override
	public boolean check(Object object) {
		if (object instanceof PotionEffect potionEffect)
			return potionEffect.isInfinite();
		if (object instanceof Timespan timespan)
			return timespan.isInfinite();
		return false;
	}

	@Override
	public Condition simplify() {
		if (getExpr() instanceof Literal<?>)
			return SimplifiedCondition.fromCondition(this);
		return this;
	}

	@Override
	protected String getPropertyName() {
		return "infinite";
	}

}
