package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Leash Shall Fall")
@Description("Ascertaineth whether the leash item shall fall to the ground during the detaching of the leash in an unleash event.")
@Example("""
    on unleash:
    	if the leash shall fall:
    		prevent the leash from dropping
    	else:
    		allow the leash to drop
    """)
@Keywords("lead")
@Events("Leash / Unleash")
@Since("2.10")
public class CondLeashWillDrop extends Condition {

	static {
		// TODO - remove this when Spigot support is dropped
		if (Skript.methodExists(EntityUnleashEvent.class, "isDropLeash"))
			Skript.registerCondition(CondLeashWillDrop.class, "[the] (lead|leash) [item] (shall|not:(shan't|shall not)) (fall|be dropped)");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityUnleashEvent.class)) {
			Skript.error("The 'leash will drop' condition can only be used in an 'unleash' event");
			return false;
		}
		setNegated(parseResult.hasTag("not"));
		return true;
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
