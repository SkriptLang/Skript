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
		"For more info, see <a href='https://minecraft.wiki/w/Slime#.22Slime_chunks.22'>the Minecraft wiki</a>."})
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
