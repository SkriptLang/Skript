package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class CondEntityFrozen extends Condition {

	static {
		if (Skript.methodExists(Server.class, "getServerTickManager")) {
			Skript.registerCondition(CondEntityFrozen.class,
				"%entities% (is|are) (server [state]|tick) frozen",
				"%entities% (is[n't| not]|are[n't| not]) (server [state]|tick) frozen");
		}
	}

	private Expression<Entity> entities;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return entities.check(e, entity -> Bukkit.getServer().getServerTickManager().isFrozen(entity), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "entity/entities " + entities.toString(event, debug) + " " + (isNegated() ? "isn't/aren't " : "is/are ") + "frozen";
	}
}
