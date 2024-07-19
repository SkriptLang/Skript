package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.jetbrains.annotations.Nullable;

@Name("Leash Will Drop")
@Description("Checks whether the leash item will drop during the leash detached in an unleash event.")
@Examples({
	"on unleash:",
		"\tif the leash will drop:",
			"\t\tprevent the leash from dropping",
		"\telse:",
			"\t\tallow the leash to drop"
})
@Keywords("lead")
@Events("Unleash")
@RequiredPlugins("Paper 1.16+")
@Since("INSERT VERSION")
public class CondLeashWillDrop extends Condition {

	static {
		if (Skript.methodExists(EntityUnleashEvent.class, "isDropLeash"))
			Skript.registerCondition(CondLeashWillDrop.class, "[the] (lead|leash) [item] (will|not:(won't|will not)) (drop|be dropped)");
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
		if (!(event instanceof EntityUnleashEvent))
			return false;
		return ((EntityUnleashEvent) event).isDropLeash() ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "leash will" + (isNegated() ? " not" : "") + " be dropped";
	}

}
