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
public class EffRegisterTeam extends Effect {

	static {
		Skript.registerEffect(EffRegisterTeam.class,
			"(create|register) [a] [new] team [named] %string% (for|in) %scoreboards%"
		);
	}

	private Expression<String> name;
	private Expression<Scoreboard> scoreboard;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result) {
		this.name = (Expression<String>) expressions[0];
		this.scoreboard = (Expression<Scoreboard>) expressions[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		String name = this.name.getSingle(event);
		if (name == null)
			return;
		for (Scoreboard scoreboard : scoreboard.getArray(event)) {
			scoreboard.registerNewTeam(name);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "register a new team named" + name.toString(event, debug)
			+ " for " + scoreboard.toString(event, debug);
	}

}
