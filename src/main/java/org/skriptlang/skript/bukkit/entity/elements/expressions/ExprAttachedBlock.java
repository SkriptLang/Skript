package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.LinkedHashSet;
import java.util.Set;

@Name("Attached/Hit Block")
@Description({
	"Returns the attached block of an arrow, or the block a raytrace hit.",
	"'attached block' only applies to projectiles, whereas 'hit block' applies to both projectiles and raytrace results.",
	"The plural version of the expression should be used for projectiles, "
		+ "as it is more reliable compared to the single version.",
	"A raytrace only ever hits one block, and has none when it hit an entity or nothing at all."
})
@Example("set hit block of last shot arrow to diamond block")
@Example("""
	on projectile hit:
		wait 1 tick
		break attached blocks of event-projectile
		kill event-projectile
	""")
@Example("""
	set {_hit} to the results of a raytrace from player for 20 meters
	if {_hit} hit a block:
		send "You are looking at %the hit block of {_hit}%"
	""")
@Since("2.8.0, 2.12 (multiple blocks), INSERT VERSION (raytrace results)")
public class ExprAttachedBlock extends PropertyExpression<Object, Block> {

	public static void register(SyntaxRegistry registry) {
		// the raytrace module may not have registered, in which case its type can't be used in a pattern
		boolean raytraceSupported = Classes.getClassInfoNoError("raytraceresult") != null;
		String hitTypes = raytraceSupported ? "projectiles/raytraceresults" : "projectiles";

		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprAttachedBlock.class, Block.class)
				.supplier(ExprAttachedBlock::new)
				.priority(PropertyExpression.DEFAULT_PRIORITY)
				.addPatterns(
					"[the] attached block[multiple:s] of %projectiles%",
					"%projectiles%'[s] attached block[multiple:s]",
					"[the] hit block[multiple:s] of %" + hitTypes + "%",
					"%" + hitTypes + "%'[s] hit block[multiple:s]")
				.build()
		);
	}

	private boolean isMultiple;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMultiple = parseResult.hasTag("multiple");
		setExpr(expressions[0]);

		// a raytrace only ever hits a single block, so the plural recommendation only concerns projectiles
		if (!isMultiple && Projectile.class.isAssignableFrom(getExpr().getReturnType())) {
			isMultiple = true;
			String expr = toString(null, Skript.debug());
			isMultiple = false;
			Skript.warning("It is recommended to use the plural version of this expression instead: '" + expr + "'");
		}

		return true;
	}

	@Override
	protected Block[] get(Event event, Object[] source) {
		Set<Block> blocks = new LinkedHashSet<>();

		for (Object object : source) {
			if (object instanceof RayTraceResult result) {
				Block hitBlock = result.getHitBlock();
				if (hitBlock != null)
					blocks.add(hitBlock);
			} else if (object instanceof AbstractArrow abstractArrow) {
				if (isMultiple) {
					blocks.addAll(abstractArrow.getAttachedBlocks());
				} else {
					Block attachedBlock = abstractArrow.getAttachedBlock();
					if (attachedBlock != null)
						blocks.add(attachedBlock);
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
