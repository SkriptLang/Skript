package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

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
@Since("2.8.0, 2.12 (multiple blocks)")
@RequiredPlugins("Minecraft 1.21.4+ (multiple blocks)")
public class ExprAttachedBlock extends PropertyExpression<Projectile, Block> {

	static {
		register(ExprAttachedBlock.class, Block.class, "(attached|hit) block[multiple:s]", "projectiles");
	}

	// TODO - remove this when only Paper 1.21.4+ is supported
	private static final boolean SUPPORTS_MULTIPLE = Skript.methodExists(AbstractArrow.class, "getAttachedBlocks");

	private boolean isMultiple;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMultiple = parseResult.hasTag("multiple");
		// noinspection unchecked
		setExpr((Expression<? extends Projectile>) expressions[0]);

		if (!SUPPORTS_MULTIPLE && isMultiple) {
			Skript.error("The plural version of this expression is only available when running Paper 1.21.4 or newer.");
			return false;
		}

		if (SUPPORTS_MULTIPLE && !isMultiple) {
			isMultiple = true;
			String expr = toString(null, Skript.debug());
			isMultiple = false;
			Skript.warning("It is recommended to use the plural version of this expression instead: '" + expr + "'");
		}

		return true;
	}

	@Override
	protected Block[] get(Event event, Projectile[] source) {
		Set<Object> blocks = new HashSet<>();

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
		return !isMultiple && getExpr().isSingle();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "attached block" + (isMultiple ? "s" : "") + " of " + getExpr().toString(event, debug);
	}

}
