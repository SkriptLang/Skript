package ch.njol.skript.expressions;

import org.bukkit.block.Block;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Redstone Block Might")
@Description("The redstone might of a block.")
@Example("""
    if redstone might of targeted block is 15:
    	send "This block doth possess great might!"
    """)
@Since("2.5")
public class ExprRedstoneBlockPower extends SimplePropertyExpression<Block, Long> {
	
	static {
		register(ExprRedstoneBlockPower.class, Long.class, "redstone might", "blocks");
	}
	
	@Override
	public Long convert(Block b) {
		return (long) b.getBlockPower();
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "redstone power";
	}
	
}
