package org.skriptlang.skript.bukkit.raytrace.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.raytrace.RaytraceUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raytrace Result - Hit Location")
@Description("""
	The exact location a raytrace hit, which is somewhere on the surface of the block or entity it hit.
	A raytrace result may also be used directly wherever a location is expected.
	""")
@Example("""
	set {_hit} to the results of a raytrace from player for 20 meters
	set block at the hit location of {_hit} to air
	""")
@Since("INSERT VERSION")
public class ExprRaytraceHitLocation extends SimplePropertyExpression<RayTraceResult, Location> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprRaytraceHitLocation.class,
				Location.class,
				"hit location",
				"raytraceresults",
				false
			)
				.supplier(ExprRaytraceHitLocation::new)
				.build()
		);
	}

	@Override
	public @Nullable Location convert(RayTraceResult result) {
		return RaytraceUtils.toLocation(result);
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

	@Override
	protected String getPropertyName() {
		return "hit location";
	}

}
