package org.skriptlang.skript.bukkit.world.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Load World")
@Description("""
	Allows you to load or unload a world.
	Note that if you use `load` a new world will be created if one does not exist with the provided name.
	When attempting to load a normal vanilla world you must define it's environment i.e "world_nether" must be loaded with nether environment.
	""")
@Example("load world \"world_nether\" with environment nether")
@Example("load the world \"myCustomWorld\"")
@Example("unload \"world_nether\"")
@Example("unload \"world_the_end\" without saving")
@Example("unload all worlds")
@Since("2.8.0")
public class EffLoadWorld extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffLoadWorld.class)
			.supplier(EffLoadWorld::new)
			.addPatterns(
				"load [the] world[s] %strings% [with environment %-environment%]",
				"unload [[the] world[s]] %worlds% [:without saving]"
			)
			.build());
	}

	private boolean save, load;
	private Expression<?> worlds;
	private @Nullable Expression<Environment> environment;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worlds = expressions[0];
		load = matchedPattern == 0;
		if (load) {
			environment = (Expression<Environment>) expressions[1];
		} else {
			save = !parseResult.hasTag("without saving");
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Environment environment = null;
		if (this.environment != null)
			environment = this.environment.getSingle(event);

		for (Object world : worlds.getArray(event)) {
			if (load && world instanceof String string) {
				WorldCreator worldCreator = new WorldCreator(string);
				if (environment != null)
					worldCreator.environment(environment);
				worldCreator.createWorld();
			} else if (!load && world instanceof World unloadWorld) {
				Bukkit.unloadWorld(unloadWorld, save);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(load ? "load" : "unload")
			.append("the world(s)", worlds)
			.appendIf(!save, "without saving")
			.appendIf(environment != null, "with environment", environment)
			.toString();
	}

}

