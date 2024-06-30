package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.scoreboard.Criterion;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

// todo doc
public class EffRegisterObjective extends Effect {

	static {
		Skript.registerEffect(EffRegisterObjective.class,
			"(create|register) a[n] %criterion% objective [named] %string% (for|in) %scoreboards%",
			"(create|register) a[n] %criterion% objective [named] %string% with display[ ]name %string% (for|in) %scoreboards%"
		);
	}

	private Expression<Criterion> criterion;
	private Expression<String> name, displayName;
	private Expression<Scoreboard> scoreboard;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result) {
		this.criterion = (Expression<Criterion>) expressions[0];
		this.name = (Expression<String>) expressions[1];
		if (pattern == 1) {
			this.displayName = (Expression<String>) expressions[2];
			this.scoreboard = (Expression<Scoreboard>) expressions[3];
		} else {
            this.displayName = name;
			this.scoreboard = (Expression<Scoreboard>) expressions[2];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Criterion criterion = this.criterion.getSingle(event);
		String name = this.name.getSingle(event), displayName = this.displayName.getOptionalSingle(event).orElse(name);
		if (criterion == null || name == null)
			return;
		for (Scoreboard scoreboard : scoreboard.getArray(event))
			criterion.registerObjective(scoreboard, name, displayName);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "register a " + criterion.toString(event, debug)
			+ " objective named " + name.toString(event, debug)
			+ " for " + scoreboard.toString(event, debug);
	}

}
