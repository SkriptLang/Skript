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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Loaded Chunks of World")
@Description("Returns a list of loaded chunks of a world.")
@Examples("set {_chunks::*} to the loaded chunks of player's world")
@Since("INSERT VERSION")
public class ExprLoadedChunks extends SimpleExpression<Chunk> {

	static {
		Skript.registerExpression(ExprLoadedChunks.class, Chunk.class, ExpressionType.COMBINED, "[all [of]] [the] loaded chunks (of|in) %worlds%");
	}

	private Expression<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected Chunk[] get(Event event) {
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
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Chunk> getReturnType() {
		return Chunk.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "loaded chunks of " + worlds.toString(event, debug);
	}

}
