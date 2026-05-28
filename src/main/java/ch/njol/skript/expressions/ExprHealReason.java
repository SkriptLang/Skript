package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.EventValues;

import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

@Name("Heal Reason")
@Description("The <a href='#healreason'>heal reason</a> of a <a href='#heal'>heal event</a>.")
@Example("""
	on heal:
		heal reason is satiated
		send "You ate enough food and gained full health back!"
	""")
@Events("heal")
@Since("2.5")
public class ExprHealReason extends EventValueExpression<RegainReason> {

	static {
		register(ExprHealReason.class, RegainReason.class, "(regen|health regain|heal[ing]) (reason|cause)");
	}

	public ExprHealReason() {
		super(RegainReason.class);
	}

	@Override
	public boolean setTime(int time) {
		if (time == EventValues.TIME_FUTURE)
			return false;
		return super.setTime(time);
	}

}
