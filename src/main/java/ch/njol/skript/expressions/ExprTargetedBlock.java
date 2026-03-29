package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Block Aimed Upon")
@Description({
	"The block upon which the crosshair doth rest. This expression regardeth all blocks that are not air as fully solid, e.g. torches shall be as a solid stone block.",
	"The actual aimed-at block shall regard the true bounding shape of the block."
})
@Example("set aimed-at block of player to stone")
@Example("set aimed-at block of player to oak_stairs[waterlogged=true]")
@Example("break aimed-at block of player using player's tool")
@Example("give player 1 of type of aimed-at block")
@Example("teleport player to location above aimed-at block")
@Example("kill all entities in radius 3 around aimed-at block of player")
@Example("set {_block} to actual aimed-at block of player")
@Example("break actual aimed-at block of player")
@Since("1.0, 2.9.0 (actual/exact)")
public class ExprTargetedBlock extends PropertyExpression<LivingEntity, Block> {

	static {
		Skript.registerExpression(ExprTargetedBlock.class, Block.class, ExpressionType.COMBINED,
				"[the] [actual:(actual[ly]|exact)] aimed[(-| )]at block[s] [of %livingentities%]", "%livingentities%'[s] [actual:(actual[ly]|exact)] aimed[(-| )]at block[s]");
	}

	private boolean actual;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		actual = parser.hasTag("actual");
		return true;
	}

	@Override
	protected Block[] get(Event event, LivingEntity[] source) {
		Integer distance = SkriptConfig.maxTargetBlockDistance.value();
		return get(source, livingEntity -> {
			Block block;
			if (actual) {
				block = livingEntity.getTargetBlockExact(distance);
			} else {
				block = livingEntity.getTargetBlock(null, distance);
			}
			if (block != null && ItemUtils.isAir(block.getType()))
				return null;
			return block;
		});
	}

	@Override
	public boolean setTime(int time) {
		super.setTime(time);
		return true;
	}

	@Override
	public Class<Block> getReturnType() {
		return Block.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String block = getExpr().isSingle() ? "block" : "blocks";
		return "the " + (this.actual ? "actual " : "") + "target " + block + " of " + getExpr().toString(event, debug);
	}

}
