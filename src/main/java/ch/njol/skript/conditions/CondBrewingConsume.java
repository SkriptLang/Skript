package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.jetbrains.annotations.Nullable;

@Name("Brewing Will Consume Fuel")
@Description({
	"Checks if the 'brewing fuel' event will consume fuel.",
	"By making it not consume, it will keep the fuel item and still add fuel to the brewing stand."
})
@Examples({
	"on brewing fuel:",
		"\tif the brewing stand will consume the fuel:",
			"\tmake the brewing stand not consume the fuel"
})
@Since("INSERT VERSION")
public class CondBrewingConsume extends Condition {

	static {
		Skript.registerCondition(CondBrewingConsume.class,
			"[the] brewing stand will consume [the] fuel",
			"[the] brewing stand (will not|won't) consume [the] fuel");
	}

	private boolean checkConsume;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BrewingStandFuelEvent.class)) {
			Skript.error("The 'brewing will consume fuel' condition only be used in a 'brewing fuel' event.");
			return false;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof BrewingStandFuelEvent brewingStandFuelEvent))
			return false;
		return brewingStandFuelEvent.isConsuming() == checkConsume;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the brewing stand will " + (checkConsume ? "" : "not") + " consume the fuel";
	}

}
