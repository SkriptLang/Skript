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

@Name("Summon or Banish a World")
@Description({
		"Summon thy worlds into existence or banish them from memory.",
		"The summoning effect shall create a new world should one not already exist.",
		"When attempting to summon a vanilla world, thou must declare its environment, e.g. \"world_nether\" must be summoned with nether environment."
})
@Example("summon world \"world_nether\" with environment nether")
@Example("summon the world \"myCustomWorld\"")
@Example("banish \"world_nether\"")
@Example("banish \"world_the_end\" without saving")
@Example("banish all worlds")
@Since("2.8.0")
public class EffWorldLoad extends Effect {

	static {
		Skript.registerEffect(EffWorldLoad.class,
				"summon [the] world[s] %strings% [with environment %-environment%]",
				"banish [[the] world[s]] %worlds% [:without saving]"
		);
	}

	private boolean save, load;
	private Expression<?> worlds;
	@Nullable
	private Expression<Environment> environment;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
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
		Environment environment = this.environment != null ? this.environment.getSingle(event) : null;
		for (Object world : worlds.getArray(event)) {
			if (load && world instanceof String) {
				WorldCreator worldCreator = new WorldCreator((String) world);
				if (environment != null)
					worldCreator.environment(environment);
				worldCreator.createWorld();
			} else if (!load && world instanceof World) {
				Bukkit.unloadWorld((World) world, save);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (load)
			return "load the world(s) " + worlds.toString(event, debug) + (environment == null ? "" : " with environment " + environment.toString(event, debug));
		return "unload the world(s) " + worlds.toString(event, debug) + " " + (save ? "with saving" : "without saving");
	}

}
