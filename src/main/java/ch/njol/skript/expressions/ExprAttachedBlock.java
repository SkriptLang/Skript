package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Arrow Attached Block")
@Description({
	"Returns the attached block of an arrow.",
	"If running Paper 1.21.4+, the plural version of the expression should be used as it is more reliable compared to the single version."
})
@Example("set hit block of last shot arrow to diamond block")
@Example("""
  on projectile hit:
  	wait 1 tick
  	break attached blocks of event-projectile
  	kill event-projectile
	""")
@Since("2.8.0, INSERT VERSION (multiple blocks)")
public class ExprAttachedBlock extends PropertyExpression<Projectile, Block> {

	static {
		Skript.registerExpression(ExprAttachedBlock.class, Block.class, ExpressionType.PROPERTY,
			"[the] (attached|hit) block[multiple:s] of %projectiles%",
			"%projectiles%'[s] (attached|hit) block[multiple:s]"
		);
	}

	// TODO - remove this when 1.21.4 is the minimum supported version
	private static final boolean SUPPORTS_MULTIPLE = Skript.methodExists(AbstractArrow.class, "getAttachedBlocks");

	private boolean isMultiple;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMultiple = parseResult.hasTag("multiple");
		setExpr((Expression<? extends Projectile>) expressions[0]);
		return true;
	}

	@Override
	protected Block[] get(Event event, Projectile[] source) {
		List<Block> blocks = new ArrayList<>();

		for (Projectile projectile : source) {
			if (projectile instanceof AbstractArrow abstractArrow) {
				if (isMultiple) {
					blocks.addAll(abstractArrow.getAttachedBlocks());
				} else {
					blocks.add(abstractArrow.getAttachedBlock());
				}
			}
		}

		return blocks.toArray(new Block[0]);
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	public boolean isSingle() {
		return !isMultiple;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "attached block" + (isMultiple ? "s" : "");
	}

}