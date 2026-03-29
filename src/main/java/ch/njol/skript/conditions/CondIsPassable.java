package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.block.Block;

@Name("Be Passable")
@Description({
	"Doth examine whether a block may be traversed without hindrance.",
	"A block is passable if it possesseth no colliding parts that would bar players from passing through.",
	"Blocks such as tall grass, flowers, signs, and the like art passable, yet open doors, fence gates, trap doors, and their ilk "
		+ "art not, for they still bear parts with which one may collide."
})
@Example("if player's targeted block is passable")
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
