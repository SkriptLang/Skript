package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
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
public class ExprDustedStage extends SimpleExpression<Integer> {

	private static final boolean SUPPORTS_DUSTING = Skript.classExists("org.bukkit.block.data.Brushable");

	static {
		if (SUPPORTS_DUSTING)
			Skript.registerExpression(ExprDustedStage.class, Integer.class, ExpressionType.SIMPLE,
				"[the] [:max[imum]] dusted (value|stage) of %blocks%",
				"%blocks%'[s] [:max[imum]] dusted (value|stage)");
	}

	private Expression<Block> blocks;
	private boolean isMax;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		blocks = (Expression<Block>) exprs[0];
		isMax = parseResult.hasTag("max");
		return true;
	}

	@Nullable
	@Override
	protected Integer[] get(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (block != null && block.getBlockData() instanceof Brushable) {
				Brushable brushableBlock = (Brushable) block.getBlockData();
				return new Integer[]{isMax ? brushableBlock.getMaximumDusted() : brushableBlock.getDusted()};
			}
		}
		return new Integer[0];
	}

	@Override
	public boolean isSingle() {
		return blocks.isSingle();
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isMax ? "maximum " : "") + blocks.toString(event, debug) + " dusted";
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET && delta.length > 0) {
			for (Block block : blocks.getArray(event)) {
				if (block != null && block.getBlockData() instanceof Brushable) {
					Brushable brushableBlock = (Brushable) block.getBlockData();
					brushableBlock.setDusted(((Integer) delta[0]).intValue());
					block.setBlockData(brushableBlock);
				}
			}
		}
	}

	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET) {
			return new Class[]{Integer.class};
		}
		return null;
	}

}
