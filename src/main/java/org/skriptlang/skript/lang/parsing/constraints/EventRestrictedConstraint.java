package org.skriptlang.skript.lang.parsing.constraints;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import org.bukkit.event.Event;
import org.skriptlang.skript.lang.parsing.ParsingContext;
import org.skriptlang.skript.registration.SyntaxInfo;

/**
 * A constraint that ensures a syntax element is only used in supported events.
 */
public class EventRestrictedConstraint implements Constraint {

	@Override
	public boolean acceptsPreInit(SyntaxInfo<?> info, SyntaxElement element, ParseResult parseResult, ParsingContext context) {
		if (element instanceof EventRestrictedSyntax eventRestrictedSyntax) {
			Class<? extends Event>[] supportedEvents = eventRestrictedSyntax.supportedEvents();
			if (!context.isCurrentEvent(supportedEvents)) {
				Skript.error("'" + parseResult.expr + "' can only be used in "
					+ EventRestrictedSyntax.supportedEventsNames(supportedEvents));
				return false;
			}
		}
		return true;
	}

	@Override
	public Lifetime lifetime() {
		return Lifetime.PERMANENT;
	}

}
