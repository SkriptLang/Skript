package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Range")
@Description({
	"The range of a beacon's effects, in blocks."
})
@Example("""
	if the beacon tier of the clicked block is 4:
		set the beacon effect range of the clicked block to 100
	"""
)
@Since("2.10")
public class ExprBeaconRange extends SimplePropertyExpression<Block, Double> {

	static {
		if (Skript.methodExists(Beacon.class, "getEffectRange"))
			register(ExprBeaconRange.class, Double.class, "beacon [effect] range", "blocks");
	}

	@Override
	public @Nullable Double convert(Block block) {
		if (block.getState() instanceof Beacon beacon)
			return beacon.getEffectRange();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Double.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (mode == ChangeMode.RESET) {
			for (Block block : getExpr().getArray(event)) {
				if (block.getState() instanceof Beacon beacon) {
					beacon.resetEffectRange();
					beacon.update(true);
				}
			}
			return;
		}

		assert delta != null;
		double range = ((Number) delta[0]).doubleValue();
		for (Block block : getExpr().getArray(event)) {
			if (block.getState() instanceof Beacon beacon) {
				switch (mode) {
					case SET -> beacon.setEffectRange(Math2.fit(0, range, Double.MAX_VALUE));
					case ADD -> beacon.setEffectRange(Math2.fit(0, beacon.getEffectRange() + range, Double.MAX_VALUE));
					case REMOVE -> beacon.setEffectRange(Math2.fit(0, beacon.getEffectRange() - range, Double.MAX_VALUE));
					default -> throw new IllegalStateException();
				}
				beacon.update(true);
			}
		}
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

	@Override
	protected String getPropertyName() {
		return "beacon range";
	}

}
