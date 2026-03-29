package ch.njol.skript.effects;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;


@Name("Halt the Server")
@Description("Doth halt or revive the server. If 'revive' be used when the restart-script spigot.yml option remaineth undefined, the server shall halt instead.")
@Example("halt the server")
@Example("revive server")
@Since("2.5")
public class EffStopServer extends Effect {
	
	static {
		Skript.registerEffect(EffStopServer.class,
			"(halt|shut[ ]down) [the] server",
			"revive [the] server");
	}
	
	private boolean restart;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		restart = matchedPattern == 1;
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		if (restart)
			Bukkit.spigot().restart();
		else
			Bukkit.shutdown();
	}
	
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (restart ? "restart" : "stop") + " the server";
	}
	
}
