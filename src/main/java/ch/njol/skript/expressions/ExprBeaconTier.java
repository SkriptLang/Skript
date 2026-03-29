package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Station")
@Description({
	"The station of a beacon. Doth range from 0 unto 4."
})
@Example("""
    if the beacon station of the clicked block is 4:
    	send "This be a beacon of the highest station!"
    """
)
@Since("2.10")
public class ExprBeaconTier extends SimplePropertyExpression<Block, Integer> {

	static {
		register(ExprBeaconTier.class, Integer.class, "beacon station", "blocks");
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof Beacon beacon)
			return beacon.getTier();
		return null;
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "beacon tier";
	}

}
