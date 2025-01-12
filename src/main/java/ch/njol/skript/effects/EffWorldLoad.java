package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Load World")
@Description({
	"Load your worlds or unload your worlds",
	"The load effect will create a new world if world doesn't already exist.",
	"When attempting to load a normal vanilla world you must define it's environment i.e \"world_nether\" must be loaded with nether environment"
})
@Examples({
	"load world \"world_nether\" with environment nether",
	"load the world \"myCustomWorld\"",
	"unload \"world_nether\"",
	"unload \"world_the_end\" without saving",
	"unload all worlds"
})
@Since("2.8.0")
public class EffWorldLoad extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffWorldLoad.class,
			"load [[the] world[s]] %strings% [with environment %-environment%]",
			"unload [[the] world[s]] %worlds% [:without saving]"
		);
	}

	private Node node;
	private boolean save, load;
	private Expression<?> worlds;
	private @Nullable Expression<Environment> environment;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		worlds = exprs[0];
		load = matchedPattern == 0;
		if (load) {
			environment = (Expression<Environment>) exprs[1];
		} else {
			save = !parseResult.hasTag("without saving");
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Environment environment = null;
		if (this.environment != null) {
			environment = this.environment.getSingle(event);
			if (environment == null)
				warning("The provided environment was not set, so defaulted to none.", this.environment.toString());
		}

		for (Object world : worlds.getArray(event)) {
			if (load && world instanceof String string) {
				WorldCreator worldCreator = new WorldCreator(string);
				if (environment != null)
					worldCreator.environment(environment);
				worldCreator.createWorld();
			} else if (!load && world instanceof World worldI) {
				Bukkit.unloadWorld(worldI, save);
			}
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (load)
			return "load the world(s) " + worlds.toString(event, debug) + (environment == null ? "" : " with environment " + environment.toString(event, debug));
		return "unload the world(s) " + worlds.toString(event, debug) + " " + (save ? "with saving" : "without saving");
	}

}
