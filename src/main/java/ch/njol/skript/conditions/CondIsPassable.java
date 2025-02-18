package ch.njol.skript.conditions;

import org.bukkit.block.Block;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;

@Name("Is Passable")
@Description({"Checks whether a block is passable.",
			"A block is passable if it has no colliding parts that would prevent players from moving through it.",
			"Blocks like tall grass, flowers, signs, etc. are passable, but open doors, fence gates, trap doors, etc. are not because they still have parts that can be collided with."
})
@Examples("if player's targeted block is passable")
@Since("2.5.1")
@RequiredPlugins("Minecraft 1.13.2+")
public class CondIsPassable extends PropertyCondition<Block> {
	
	static {
		if (Skript.methodExists(Block.class, "isPassable")) {
			register(CondIsPassable.class, "passable", "blocks");
		}
	}
	
	@Override
	public boolean check(Block block) {
		return block.isPassable();
	}
	
	@Override
	protected String getPropertyName() {
		return "passable";
	}
	
}
