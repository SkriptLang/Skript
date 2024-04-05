/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
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
import org.eclipse.jdt.annotation.Nullable;

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
public class EffWorldLoad extends Effect {

	static {
		Skript.registerEffect(EffWorldLoad.class,
				"load [[the] world[s]] %strings% [with environment %-environment%]",
				"unload [[the] world[s]] %worlds% [:without saving]"
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
