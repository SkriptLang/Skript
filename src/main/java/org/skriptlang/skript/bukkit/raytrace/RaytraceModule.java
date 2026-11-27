package org.skriptlang.skript.bukkit.raytrace;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.raytrace.elements.conditions.CondRaytraceHit;
import org.skriptlang.skript.bukkit.raytrace.elements.effects.EffRaytraceIgnore;
import org.skriptlang.skript.bukkit.raytrace.elements.effects.EffRaytraceIgnoreTargets;
import org.skriptlang.skript.bukkit.raytrace.elements.expressions.*;
import org.skriptlang.skript.bukkit.raytrace.elements.sections.SecRaytraceIgnoreIf;
import org.skriptlang.skript.lang.converter.Converters;

/**
 * Raytracing syntax.
 */
public class RaytraceModule extends HierarchicalAddonModule {

	public RaytraceModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		// no serializer, as a result refers to a live block or entity and can't meaningfully be persisted
		Classes.registerClass(new ClassInfo<>(RayTraceResult.class, "raytraceresult")
			.user("ray ?trace ?results?")
			.name("Raytrace Result")
			.description("The result of a raytrace, holding what was hit and where.")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(RayTraceResult result, int flags) {
					Entity entity = result.getHitEntity();
					if (entity != null)
						return "raytrace result hitting " + Classes.toString(entity);
					Block block = result.getHitBlock();
					if (block != null)
						return "raytrace result hitting " + Classes.toString(block);
					return "raytrace result hitting nothing";
				}

				@Override
				public String toVariableNameString(RayTraceResult result) {
					return toString(result, 0);
				}
			})
		);

		// allows a result to be used wherever a location is expected
		Converters.registerConverter(RayTraceResult.class, Location.class, RaytraceUtils::toLocation);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			ExprSecRaytrace::register,
			ExprRaytraceRaySize::register,
			ExprRaytraceMaxDistance::register,
			EffRaytraceIgnore::register,
			EffRaytraceIgnoreTargets::register,
			SecRaytraceIgnoreIf::register,
			ExprRaytraceHitLocation::register,
			ExprRaytraceHitFace::register,
			ExprRaytraceHitEntity::register,
			CondRaytraceHit::register
		);
	}

	@Override
	public String name() {
		return "raytrace";
	}

}
