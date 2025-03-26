package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.block.Block;

@Name("Is Passable")
@Description({
	"Checks whether a block is passable.",
	"A block is passable if it has no colliding parts that would prevent players from moving through it.",
	"Blocks like tall grass, flowers, signs, etc. are passable, but open doors, fence gates, trap doors, etc. "
		+ "are not because they still have parts that can be collided with."
})
@Examples("if player's targeted block is passable")
@Since("2.5.1")
public class CondIsPassable extends PropertyCondition<Block> {
	
	static {
		register(CondIsPassable.class, "passable", "blocks");
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
