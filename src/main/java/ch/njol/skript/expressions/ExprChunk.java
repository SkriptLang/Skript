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
package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Chunk")
@Description("Returns the <a href='./classes.html#chunk'>chunk</a> of a block, location or entity is in, or a list of the loaded chunks of a world.")
@Examples({
	"add the chunk at the player to {protected chunks::*}",
	"set {_chunks::*} to the loaded chunks of the player's world"
})
@Since("2.0, INSERT VERSION (loaded chunks)")
public class ExprChunk extends SimpleExpression<Chunk> {
	
	static {
		Skript.registerExpression(ExprChunk.class, Chunk.class, ExpressionType.COMBINED,
			"[the] chunk[s] (of|%-directions%) %locations%",
			"%locations%'[s] chunk[s]",
			"[all [of]] [the] loaded chunks (of|in) %worlds%"
		);
	}

	private int pattern;
	private Expression<Location> locations;
	private Expression<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		pattern = matchedPattern;
		if (pattern == 0) {
			locations = (Expression<Location>) exprs[1];
			if (exprs[0] != null) {
				locations = Direction.combine((Expression<? extends Direction>) exprs[0], locations);
			}
		} else if (pattern == 1) {
			locations = (Expression<Location>) exprs[0];
		} else {
			worlds = ((Expression<World>) exprs[0]);
		}
		return true;
	}

	@Override
	protected @Nullable Chunk[] get(Event event) {
		if (pattern != 2) {
			return locations.stream(event)
				.map(Location::getChunk)
				.toArray(Chunk[]::new);
		}
		World[] worlds = this.worlds.getArray(event);
		if (worlds.length == 0)
			return new Chunk[0];
		List<Chunk> chunks = new ArrayList<>();
		for (World world : worlds) {
			chunks.addAll(Arrays.asList(world.getLoadedChunks()));
		}
		return chunks.toArray(new Chunk[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.RESET)
			return new Class[0];
		return null;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert mode == ChangeMode.RESET;
		Chunk[] chunks = get(event);
		for (Chunk chunk : chunks)
			chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Chunk> getReturnType() {
		return Chunk.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (pattern == 2)
			return "the loaded chunks of " + worlds.toString(event, debug);
		return "the chunk at " + locations.toString(event, debug);
	}

}
