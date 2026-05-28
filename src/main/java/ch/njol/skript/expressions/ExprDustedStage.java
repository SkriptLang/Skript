package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Brushable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Brushing Stage")
@Description("""
	Represents how far the block has been uncovered.
	The only blocks that can currently be "brushed" are Suspicious Gravel and Suspicious Sand.
	0 means the block is untouched, the max (usually 3) means nearly fulled brushed.
	Resetting this value will set it to 0.
	""")
@Example("""
	# prevent dusting past level 1
	on player change block:
		if dusting progress of future event-blockdata > 1:
			cancel event
	""")
@Example("""
	# draw particles when dusting is complete!
	on player change block:
		if brushing progress of event-block is max brushing stage of event-block:
			draw 20 totem of undying particles at event-block
	""")
@Since("2.12")
@Keywords({"brush", "brushing", "dusting"})
public class ExprDustedStage extends PropertyExpression<Object, Integer> {

	static {
		register(ExprDustedStage.class, Integer.class,
			"[:max[imum]] (dust|brush)[ed|ing] (value|stage|progress[ion])",
			"blocks/blockdatas");
	}

	private boolean isMax;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		isMax = parseResult.hasTag("max");
		return true;
	}

	@Override
	protected Integer @Nullable [] get(Event event, Object[] source) {
		return get(source, obj -> {
			Brushable brushable = getBrushable(obj);
			if (brushable != null) {
				return isMax ? brushable.getMaximumDusted() : brushable.getDusted();
			}
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (isMax) {
			Skript.error("Attempting to modify the max dusted stage is not supported.");
			return null;
		}

		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (isMax) return;
		int value = delta == null ? 0 : (Integer) delta[0];

		for (Object obj : getExpr().getArray(event)) {
			Brushable brushable = getBrushable(obj);
			if (brushable == null)
				continue;

			int currentValue = brushable.getDusted();
			int maxValue = brushable.getMaximumDusted();
			long newValue = switch (mode) {
				case SET, RESET -> value;
				case ADD -> Math2.addClamped(currentValue, value);
				case REMOVE -> Math2.addClamped(currentValue, -value);
				default -> throw new IllegalArgumentException("Change mode " + mode + " is not valid for ExprDustedStage!");
			};
			brushable.setDusted(Math.clamp(newValue, 0, maxValue));
			if (obj instanceof Block block) {
				block.setBlockData(brushable);
			}
		}
	}

	@Nullable
	private Brushable getBrushable(Object obj) {
		if (obj instanceof Block block) {
			BlockData blockData = block.getBlockData();
			if (blockData instanceof Brushable brushable)
				return brushable;
		} else if (obj instanceof Brushable brushable) {
			return brushable;
		}
		return null;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + "'s " + (isMax ? "maximum " : "") + " dusted stage";
	}

}
