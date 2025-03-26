package ch.njol.skript.expressions;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.EventValues;

@Name("Damage Cause")
@Description("The <a href='./classes.html#damagecause'>damage cause</a> of a damage event. Please click on the link for more information.")
@Examples("damage cause is lava, fire or burning")
@Since("2.0")
public class ExprDamageCause extends EventValueExpression<DamageCause> {

	static {
		register(ExprDamageCause.class, DamageCause.class, "damage (cause|type)");
	}

	public ExprDamageCause() {
		super(DamageCause.class);
	}

	@Override
	public boolean setTime(int time) {
		return time != EventValues.TIME_FUTURE; // allow past and present
	}

}
