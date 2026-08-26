package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;

@Name("Is Charged")
@Description("Checks if a creeper, wither, or wither skull is charged (powered).")
@Example("""
	if the last spawned creeper is charged:
		broadcast "A charged creeper is at %location of last spawned creeper%"
	""")
@Since("2.5, 2.10 (withers, wither skulls)")
public class CondIsCharged extends PropertyCondition<Entity> {

	static {
		register(CondIsCharged.class, "(charged|powered)", "entities");
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
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(Entity entity, boolean charged, ChangeMode mode) {
		if (entity instanceof Creeper creeper) {
			creeper.setPowered(charged);
		} else if (entity instanceof WitherSkull witherSkull) {
			witherSkull.setCharged(charged);
		}
	}

	@Override
	protected String getPropertyName() {
		return "charged";
	}

}
