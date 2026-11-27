package org.skriptlang.skript.bukkit.raytrace.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raytrace Result - Hit Entity")
@Description("""
	The entity a raytrace hit.
	This is not set when the raytrace hit a block, or when it hit nothing at all.
	""")
@Example("""
	set {_hit} to the results of a raytrace from player for 20 meters
	if {_hit} hit an entity:
		damage the hit entity of {_hit} by 5
	""")
@Since("INSERT VERSION")
public class ExprRaytraceHitEntity extends SimplePropertyExpression<RayTraceResult, Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprRaytraceHitEntity.class,
				Entity.class,
				"hit entity",
				"raytraceresults",
				false
			)
				.supplier(ExprRaytraceHitEntity::new)
				.build()
		);
	}

	@Override
	public @Nullable Entity convert(RayTraceResult result) {
		return result.getHitEntity();
	}

	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "hit entity";
	}

}
