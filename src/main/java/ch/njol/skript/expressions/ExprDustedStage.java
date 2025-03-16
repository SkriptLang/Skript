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
import org.bukkit.block.data.Brushable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dusted Stage")
@Description({
	"Represents how far the block has been uncovered.",
	"The only blocks that can currently be \"dusted\" are Suspicious Gravel and Suspicious Sand."
})
@Examples({
	"send target block's maximum dusted stage",
	"set {_sand}'s dusted stage to 2"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20+")
public class ExprDustedStage extends PropertyExpression<Block, Integer> {

	private static final boolean SUPPORTS_DUSTING = Skript.classExists("org.bukkit.block.data.Brushable");

	static {
		if (SUPPORTS_DUSTING)
			register(ExprDustedStage.class, Integer.class,
				"[:max[imum]] dust[ed|ing] (value|stage|progress[ion])",
				"blocks");
	}

	private boolean isMax;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Block>) exprs[0]);
		isMax = parseResult.hasTag("max");
		return true;
	}

	@Override
	protected Integer @Nullable [] get(Event event, Block[] source) {
		return get(source, block -> {
			if (block.getBlockData() instanceof Brushable brushable) {
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
		} else if (mode == ChangeMode.SET) {
			return CollectionUtils.array(Integer.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (isMax || mode != ChangeMode.SET || delta.length == 0)
			return;

		int value = (Integer) delta[0];
		for (Block block : getExpr().getArray(event)) {
			if (block.getBlockData() instanceof Brushable brushable) {
				brushable.setDusted(value);
				block.setBlockData(brushable);
			}
		}
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
