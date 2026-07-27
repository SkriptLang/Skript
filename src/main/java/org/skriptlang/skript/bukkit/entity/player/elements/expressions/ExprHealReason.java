package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

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

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprHealReason.class, RegainReason.class)
			.supplier(ExprHealReason::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPattern("(regen|health regain|heal[ing]) (reason|cause)")
			.build());
	}

	public ExprHealReason() {
		super(RegainReason.class);
	}

	@Override
	public boolean setTime(int time) {
		if (time == EventValue.Time.FUTURE.value())
			return false;

		return super.setTime(time);
	}

}
