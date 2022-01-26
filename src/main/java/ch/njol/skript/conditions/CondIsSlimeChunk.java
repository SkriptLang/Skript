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
package ch.njol.skript.conditions;

import java.util.Random;

import ch.njol.skript.Skript;
import org.bukkit.Chunk;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

/**
 * @author Nicofisi
 */
@Name("Is Slime Chunk")
@Description({"Tests whether a chunk is a so-called slime chunk.",
		"Slimes can generally spawn in the swamp biome and in slime chunks.",
		"For more info, see <a href='https://minecraft.gamepedia.com/Slime#.22Slime_chunks.22'>the Minecraft wiki</a>."})
@Examples({"command /slimey:",
		"\ttrigger:",
		"\t\tif chunk at player is a slime chunk:",
		"\t\t\tsend \"Yeah, it is!\"",
		"\t\telse:",
		"\t\t\tsend \"Nope, it isn't\""})
@Since("2.3")
public class CondIsSlimeChunk extends PropertyCondition<Chunk> {

	private static final boolean CHUNK_METHOD_EXISTS = Skript.methodExists(Chunk.class, "isSlimeChunk");
	
	static {
		register(CondIsSlimeChunk.class, "([a] slime chunk|slime chunks|slimey)", "chunk");
	}
	
	@Override
	public boolean check(Chunk chunk) {
		if (CHUNK_METHOD_EXISTS)
			return chunk.isSlimeChunk();

		Random random = new Random(chunk.getWorld().getSeed() +
				(0x4c1906L * chunk.getX() * chunk.getX()) +
				(0x5ac0dbL * chunk.getX()) +
				(0x4307a7L * chunk.getZ() * chunk.getZ()) +
				((0x5f24fL *chunk.getZ()) ^ 0x3ad8025fL));
		return random.nextInt(10) == 0;
	}
	
	@Override
	protected String getPropertyName() {
		return "slime chunk";
	}
	
}
