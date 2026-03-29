package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.Chunk;

@Name("Be a Slime Chunk")
@Description({
	"Determineth whether a chunk be a so-called slime chunk.",
	"Slimes may generally spawn within the swamp biome and within slime chunks.",
	"For further knowledge, consult <a href='https://minecraft.wiki/w/Slime#.22Slime_chunks.22'>the Minecraft wiki</a>."
})
@Example("""
    command /slimey:
    	trigger:
    		if chunk at player is a slime chunk:
    			send "Aye, verily it is!"
    		else:
    			send "Nay, it is not"
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
