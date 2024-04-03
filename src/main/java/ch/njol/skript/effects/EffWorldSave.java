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
import org.bukkit.World;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Save World")
@Description({
	"Save all worlds or a given world manually.",
	"Note: saving many worlds at once may possibly cause the server to freeze."
})
@Examples({
	"save \"world_nether\"",
	"save all worlds"
})
@Since("2.8.0")
public class EffWorldSave extends Effect {

	static {
		Skript.registerEffect(EffWorldSave.class, "save [[the] world[s]] %worlds%");
	}

	private Expression<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (World world : worlds.getArray(event))
			world.save();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "save the world(s) " + worlds.toString(event, debug);
	}

}
