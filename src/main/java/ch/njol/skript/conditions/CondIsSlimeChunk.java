package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.Chunk;

@Name("Is Slime Chunk")
@Description({
	"Tests whether a chunk is a so-called slime chunk.",
	"Slimes can generally spawn in the swamp biome and in slime chunks.",
	"For more info, see <a href='https://minecraft.wiki/w/Slime#.22Slime_chunks.22'>the Minecraft wiki</a>."
})
@Example("""
	command /slimey:
		trigger:
			if chunk at player is a slime chunk:
				send "Yeah, it is!"
			else:
				send "Nope, it isn't"
	""")
@Since("2.3")
public class CondIsSlimeChunk extends PropertyCondition<Chunk> {
	
	static {
		register(CondIsSlimeChunk.class, "([a] slime chunk|slime chunks|slimey)", "chunk");
	}
	
	@Override
	public boolean check(Chunk chunk) {
		return chunk.isSlimeChunk();
	}
	
	@Override
	protected String getPropertyName() {
		return "slime chunk";
	}
	
}
