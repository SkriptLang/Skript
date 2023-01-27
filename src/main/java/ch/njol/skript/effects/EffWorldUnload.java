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
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Unload World")
@Description("Unload a given world with optional saving")
@Examples({
	"unload \"world_nether\"",
	"unload \"world_the_end\" without saving",
	"unload all worlds"
})
@Since("INSERT VERSION")
public class EffWorldUnload extends Effect {

	static {
		Skript.registerEffect(EffWorldUnload.class, "unload %worlds% [:without saving]");
	}

	private boolean save;
	private Expression<World> worlds;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		save = !parseResult.hasTag("without saving");
		return true;
	}

	@Override
	protected void execute(Event event) {
		World mainWorld = Bukkit.getWorlds().get(0);
		for (World world : this.worlds.getArray(event)) {
			Bukkit.unloadWorld(world, save);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "unload world(s) " + worlds.toString(event, debug) + " " + (save ? "with saving" : "without saving");
	}

}
