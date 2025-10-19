package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Brushable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dusted Stage")
@Description({
	"Represents how far the block has been uncovered.",
	"The only blocks that can currently be \"dusted\" are Suspicious Gravel and Suspicious Sand."
})
@Example("send target block's maximum dusted stage")
@Example("set {_sand}'s dusted stage to 2")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20+")
public class ExprDustedStage extends PropertyExpression<Object, Integer> {

	private static final boolean SUPPORTS_DUSTING = Skript.classExists("org.bukkit.block.data.Brushable");

	static {
		if (SUPPORTS_DUSTING)
			register(ExprDustedStage.class, Integer.class,
				"[:max[imum]] dust[ed|ing] (value|stage|progress[ion])",
				"blocks/blockdatas");
	}

	private boolean isMax;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Block>) exprs[0]);
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
		Integer value = (delta != null && delta.length > 0) ? (Integer) delta[0] : null;

		for (Object obj : getExpr().getArray(event)) {
			Brushable brushable = getBrushable(obj);
			if (brushable == null)
				continue;

			int currentValue = brushable.getDusted();
			int maxValue = brushable.getMaximumDusted();
			int newValue = currentValue;

			switch (mode) {
				case SET -> {
					if (value != null) {
						newValue = value;
					}
				}
				case ADD -> {
					if (value != null) {
						newValue = currentValue + value;
					}
				}
				case REMOVE -> {
					if (value != null) {
						newValue = currentValue - value;
					}
				}
				case RESET -> newValue = 0;
				default -> {
					return;
				}
			}

			newValue = Math.max(0, Math.min(newValue, maxValue));

			brushable.setDusted(newValue);
			if (obj instanceof Block) {
				((Block) obj).setBlockData(brushable);
			}
		}
	}

	@Nullable
	private Brushable getBrushable(Object obj) {
		if (obj instanceof Block block) {
			BlockData blockData = block.getBlockData();
			if (blockData instanceof Brushable)
				return (Brushable) blockData;
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
