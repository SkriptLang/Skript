package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.jetbrains.annotations.Nullable;

@Name("Will Consume Item")
@Description("Returns whether the shot item will be consumed in an entity shoot bow event.")
@Example("""
	on player shoot bow;
		if the item will be consumed:
			send action bar "-1 Arrow" to shooter
	""")
@Since("INSERT VERSION")
public class CondWillConsume extends Condition implements EventRestrictedSyntax {

	static {
		Skript.registerCondition(CondWillConsume.class,
			"[the] item will be consumed",
			"[the] item (will not|won't) be consumed");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EntityShootBowEvent shootBowEvent))
			return false;
		return shootBowEvent.shouldConsumeItem();
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EntityShootBowEvent.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (isNegated())
			return "the item will not be consumed";
		return "the item will be consumed";
	}
}
