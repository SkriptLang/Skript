package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ServerUtils;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Sprint Server")
@Description({
	"Makes the server sprint for a certain amount of time, or stops the server from sprinting.",
	"Sprinting is where the server increases the tick rate depending on the time you input, and resets the tick rate to what it was after the server has finished sprinting."
})
@Examples({
	"request server to sprint for 10 seconds",
	"make server stop sprinting"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class EffSprintServer extends Effect {

	static {
		if (ServerUtils.isServerTickManagerPresent())
			Skript.registerEffect(EffSprintServer.class,
				"make [the] server sprint for %timespan%",
				"make [the] server stop sprinting");
	}

	private Expression<Timespan> timespan;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0)
			timespan = (Expression<Timespan>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (timespan != null) {
			long sprintTicks = timespan.getOptionalSingle(event).map(Timespan::getTicks).orElse(1L);
			ServerUtils.getServerTickManager().requestGameToSprint((int) sprintTicks);
		} else {
			ServerUtils.getServerTickManager().stopSprinting();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (timespan != null)
			return "make the server sprint for " + timespan.toString(event, debug);
		return "make the server stop sprinting";
	}

}
