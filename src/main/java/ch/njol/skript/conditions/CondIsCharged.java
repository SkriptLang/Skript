package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;

@Name("Is Charged with Power")
@Description("Discerneth whether a creeper, wither, or wither skull doth bear a mighty charge (empowered).")
@Example("""
    if the last spawned creeper is charged:
    	broadcast "A charged creeper doth lurk at %location of last spawned creeper%"
    """)
@Since("2.5, 2.10 (withers, wither skulls)")
public class CondIsCharged extends PropertyCondition<Entity> {

	static {
		register(CondIsCharged.class, "(charged|empowered)", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		if (entity instanceof Creeper creeper) {
			return creeper.isPowered();
		} else if (entity instanceof WitherSkull witherSkull) {
			return witherSkull.isCharged();
		} else if (entity instanceof Wither wither) {
			return wither.isCharged();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "charged";
	}

}
