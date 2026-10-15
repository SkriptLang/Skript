package org.skriptlang.skript.bukkit.raytrace.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.BlockFace;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raytrace Result - Hit Face")
@Description("""
	The side a raytrace hit, being the face of the block or of the entity's hitbox that the ray entered.
	A ray travelling east, for example, enters whatever it hits on the west side.
	""")
@Example("""
	set {_hit} to the results of a raytrace from player for 20 meters
	send "You are looking at the %hit face of {_hit}% of the block"
	""")
@Since("INSERT VERSION")
public class ExprRaytraceHitFace extends SimplePropertyExpression<RayTraceResult, BlockFace> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprRaytraceHitFace.class,
				BlockFace.class,
				"hit [block] face",
				"raytraceresults",
				false
			)
				.supplier(ExprRaytraceHitFace::new)
				.build()
		);
	}

	@Override
	public @Nullable BlockFace convert(RayTraceResult result) {
		return result.getHitBlockFace();
	}

	@Override
	public Class<BlockFace> getReturnType() {
		return BlockFace.class;
	}

	@Override
	protected String getPropertyName() {
		return "hit face";
	}

}
