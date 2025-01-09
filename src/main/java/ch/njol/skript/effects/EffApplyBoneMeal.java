package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Apply Bone Meal")
@Description("Applies bone meal to a crop, sapling, or composter")
@Examples("apply 3 bone meal to event-block")
@RequiredPlugins("Minecraft 1.16.2+")
@Since("2.8.0")
public class EffApplyBoneMeal extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffApplyBoneMeal.class, "apply [%-number%] bone[ ]meal[s] [to %blocks%]");
	}

	private Node node;
	private @Nullable Expression<Number> amount;
	private Expression<Block> blocks;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		amount = (Expression<Number>) exprs[0];
		blocks = (Expression<Block>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		int times = 1;
		if (amount != null) {
			Number amount = this.amount.getSingle(event);
			if (amount == null) {
				warning("The provided amount of bone meal was not set, so defaulted to 1.", this.amount.toString());
			} else {
				times = amount.intValue();
			}
		}

		for (Block block : blocks.getArray(event)) {
			for (int i = 0; i < times; i++) {
				block.applyBoneMeal(BlockFace.UP);
			}
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "apply " + (amount != null ? amount.toString(event, debug) + " " : "") + "bone meal to " + blocks.toString(event, debug);
	}

}
