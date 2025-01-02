package ch.njol.skript.expressions;

import org.bukkit.block.Block;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Redstone Block Power")
@Description("Power of a redstone block")
@Examples({"if redstone power of targeted block is 15:",
	"\tsend \"This block is very powerful!\""})
@Since("2.5")
public class ExprRedstoneBlockPower extends SimplePropertyExpression<Block, Long> {
	
	static {
		register(ExprRedstoneBlockPower.class, Long.class, "redstone power", "blocks");
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
