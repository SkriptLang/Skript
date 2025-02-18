package ch.njol.skript.conditions;

import org.bukkit.potion.PotionEffect;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

// This class can be expanded apon for other types if needed.
@Name("Is Infinite")
@Description("Checks whether potion effects are infinite.")
@Examples("all of the active potion effects of the player are infinite")
@Since("2.7")
public class CondIsInfinite extends PropertyCondition<PotionEffect> {

	static {
		if (Skript.methodExists(PotionEffect.class, "isInfinite"))
			register(CondIsInfinite.class, "infinite", "potioneffects");
	}

	@Override
	public boolean check(PotionEffect potion) {
		return potion.isInfinite();
	}

	@Override
	protected String getPropertyName() {
		return "infinite";
	}

}
