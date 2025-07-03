package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Tier")
@Description({
	"The tier of a beacon. Ranges from 0 to 4."
})
@Example("""
	if the beacon tier of the clicked block is 4:
		send "This is a max tier beacon!"
	"""
)
@Since("2.10")
public class ExprBeaconTier extends SimplePropertyExpression<Block, Integer> {

	static {
		register(ExprBeaconTier.class, Integer.class, "beacon tier", "blocks");
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
