package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

@Name("Block")
@Description({
	"The block involved in the event, e.g. the clicked block or the placed block.",
	"Can optionally include a direction as well, e.g. 'block above' or 'block in front of the player'."
})
@Examples({
	"block is ore",
	"set block below to air",
	"spawn a creeper above the block",
	"loop blocks in radius 4:",
	"	loop-block is obsidian",
	"	set loop-block to water",
	"block is a chest:",
	"	clear the inventory of the block"
	})
@Since("1.0")
public class ExprBlock extends WrapperExpression<Block> {

	static {
		Skript.registerExpression(ExprBlock.class, Block.class, ExpressionType.SIMPLE, "[the] [event-]block");
		Skript.registerExpression(ExprBlock.class, Block.class, ExpressionType.COMBINED, "[the] block %direction% [%location%]");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (exprs.length > 0) {
			setExpr(new ConvertedExpression<>(Direction.combine((Expression<? extends Direction>) exprs[0],
					(Expression<? extends Location>) exprs[1]), Block.class,
					new ConverterInfo<>(Location.class, Block.class, new Converter<Location, Block>() {
				@Override
				public Block convert(Location location) {
					return location.getBlock();
				}
			}, 0)));
			return true;
		} else {
			setExpr(new EventValueExpression<>(Block.class));
			return ((EventValueExpression<Block>) getExpr()).init(matchedPattern, isDelayed, parser);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr() instanceof EventValueExpression ? "the block" : "the block " + getExpr().toString(event, debug);
	}

}
