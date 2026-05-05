package ch.njol.skript.conditions;

import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Leash Will Drop")
@Description("Checks whether the leash item will drop during the leash detaching in an unleash event.")
@Example("""
	on unleash:
		if the leash will drop:
			prevent the leash from dropping
		else:
			allow the leash to drop
	""")
@Keywords("lead")
@Events("Leash / Unleash")
@Since("2.10")
public class CondLeashWillDrop extends Condition implements EventRestrictedSyntax {

	static {
		// TODO - remove this when Spigot support is dropped
		if (Skript.methodExists(EntityUnleashEvent.class, "isDropLeash"))
			Skript.registerCondition(CondLeashWillDrop.class, "[the] (lead|leash) [item] (will|not:(won't|will not)) (drop|be dropped)");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.hasTag("not"));
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EntityUnleashEvent.class);
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EntityUnleashEvent unleashEvent))
			return false;
		return unleashEvent.isDropLeash() ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the leash will" + (isNegated() ? " not" : "") + " be dropped";
	}

}
