package org.skriptlang.skript.bukkit.block.sign.elements.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.sign.Side;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Changed Sign Side")
@Description("Checks whether a side was changed in a sign change event.")
@Example("""
	on sign change:
		the back side was changed
		cancel the event
		send "<red>It is not possible to change the back side of a sign!" to the player
	""")
@Since("INSERT VERSION")
@Events("sign change")
public class CondChangedSide extends Condition implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.CONDITION, SyntaxInfo.simple(CondChangedSide.class, CondChangedSide::new,
			"[the] (front|:back) [side] (is|was) changed",
			"[the] (front|:back) [side] (is not|isn't|was not|wasn't) changed",
			"[the] changed side (is|was) [the] (front|:back)",
			"[the] changed side (is not|isn't|was not|wasn't) [the] (front|:back)"));
	}

	private Side expectedSide;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expectedSide = parseResult.hasTag("back") ? Side.BACK : Side.FRONT;
		setNegated(matchedPattern % 2 == 1);
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{SignChangeEvent.class};
	}

	@Override
	public boolean check(Event event) {
		return event instanceof SignChangeEvent signChangeEvent &&
			(signChangeEvent.getSide() == expectedSide) != isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + (expectedSide == Side.FRONT ? "front" : "back") + " side was " + (isNegated() ? "not " : "") + "changed";
	}

}
