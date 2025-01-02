package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import io.papermc.paper.event.block.BeaconActivatedEvent;
import io.papermc.paper.event.block.BeaconDeactivatedEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Toggle")
@Description("Called when a beacon is activated or deactivated.")
@Examples({
	"on beacon toggle:",
	"on beacon activate:",
	"on beacon deactivate:"
})
@RequiredPlugins("Paper")
@Since("2.10")
public class EvtBeaconToggle extends SkriptEvent {

	static {
		if (Skript.classExists("io.papermc.paper.event.block.BeaconActivatedEvent"))
			Skript.registerEvent("Beacon Toggle", EvtBeaconToggle.class, new Class[] {BeaconActivatedEvent.class, BeaconDeactivatedEvent.class},
					"beacon toggle",
					"beacon activat(e|ion)",
					"beacon deactivat(e|ion)");
	}

	private boolean isActivate, isToggle;

	@Override
    public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		isToggle = matchedPattern == 0;
		isActivate = matchedPattern == 1;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!isToggle) {
			if (event instanceof BeaconActivatedEvent) {
				return isActivate;
			} else if (event instanceof BeaconDeactivatedEvent) {
				return !isActivate;
			}
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "beacon " + (isToggle ? "toggle" : isActivate ? "activate" : "deactivate");
	}

}
