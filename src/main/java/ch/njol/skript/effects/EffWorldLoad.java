package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Load World")
@Description("""
	Load or unload a world.
	Loading a world that does not already exist will create a new one.
	When attempting to load a normal vanilla world, you must define it's environment. Such as "world_nether" would need \
	to be loaded with the 'nether' environment.
	
	See also: the generic 'unload' effect, which will save and unload a world.
	""")
@Example("load world \"world_nether\" with environment nether")
@Example("load the world \"myCustomWorld\"")
@Example("unload \"world_the_end\" without saving")
@Since("2.8.0")
public class EffWorldLoad extends Effect {

	static {
		Skript.registerEffect(EffWorldLoad.class,
				"load [the] world[s] %strings% [with environment %-environment%]",
				"unload [[the] world[s]] %worlds% without saving"
		);
	}

	private boolean load;
	private Expression<String> strings;
	private Expression<World> worlds;
	private @Nullable Expression<Environment> environment;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		load = matchedPattern == 0;
		if (load) {
			//noinspection unchecked
			strings = (Expression<String>) exprs[0];
			//noinspection unchecked
			environment = (Expression<Environment>) exprs[1];
		} else {
			//noinspection unchecked
			worlds = (Expression<World>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (load) {
			Environment environment = this.environment != null ? this.environment.getSingle(event) : null;
			for (String string : strings.getArray(event)) {
				WorldCreator worldCreator = new WorldCreator(string);
				if (environment != null)
					worldCreator.environment(environment);
				worldCreator.createWorld();
			}
		} else {
			for (World world : worlds.getArray(event))
				Bukkit.unloadWorld(world, false);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (load)
			return "load the world(s) " + worlds.toString(event, debug) + (environment == null ? "" : " with environment " + environment.toString(event, debug));
		return "unload the world(s) " + worlds.toString(event, debug) + " without saving";
	}

}
