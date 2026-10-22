package org.skriptlang.skript.bukkit.raytrace.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.FluidCollisionMode;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.raytrace.RaytraceConfig;
import org.skriptlang.skript.bukkit.raytrace.RaytraceConfig.BlockCollisionMode;
import org.skriptlang.skript.bukkit.raytrace.RaytraceConfig.EntityCollisionMode;
import org.skriptlang.skript.bukkit.raytrace.RaytraceSectionEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raytrace - Ignore Blocks/Entities/Fluids")
@Description("""
	Tells the raytrace this section belongs to what it should and should not collide with.
	'ignore passable blocks' only skips blocks a player can walk through, such as grass, \
	whereas 'ignore all blocks' skips every block.
	'ignore flowing fluids' collides only with fluid source blocks, whereas 'ignore all fluids' skips fluids entirely.
	'unignore' restores the default for blocks, entities or fluids, so that the ray collides with them again. \
	It is useful for undoing an earlier 'ignore' within a conditional.
	A raytrace that ignores both blocks and entities can't hit anything, and will error when run.
	
	For more specific filtering, use 'ignore entities if' or 'ignore blocks if' section, which can filter based on any condition.
	
	Can only be used within a raytrace section.
	""")
@Example("""
	set {_hit} to the results of a raytrace from {_start} along {_direction}:
		ignore passable blocks
		ignore flowing fluids
	""")
@Example("""
	set {_hit} to the results of a raytrace from {_start} along {_direction}:
		# only blocks can be hit
		ignore all entities
	""")
@Example("""
	set {_hit} to the results of a raytrace from {_start} along {_direction}:
		ignore all entities
		if {_include mobs} is true:
			# entities can be hit after all
			unignore all entities
	""")
@Since("INSERT VERSION")
public class EffRaytraceIgnore extends Effect implements EventRestrictedSyntax {

	private static final int BLOCKS = 0, ENTITIES = 1, FLUIDS = 2;

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffRaytraceIgnore.class)
				.supplier(EffRaytraceIgnore::new)
				.addPatterns(
					"[:un]ignore [all|:passable] blocks",
					"[:un]ignore [all] entities",
					"[:un]ignore [all|:flowing] fluids")
				.build()
		);
	}

	private int target;
	private boolean partial;
	private boolean unignore;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		target = matchedPattern;
		partial = parseResult.hasTag("passable") || parseResult.hasTag("flowing");
		unignore = parseResult.hasTag("un");
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(RaytraceSectionEvent.class);
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof RaytraceSectionEvent sectionEvent))
			return;
		RaytraceConfig config = sectionEvent.getConfig();
		// unignoring restores the default, so that the target is collided with again
		switch (target) {
			case BLOCKS -> config.blockCollisionMode = unignore ? BlockCollisionMode.ALWAYS
				: (partial ? BlockCollisionMode.IGNORE_PASSABLES : BlockCollisionMode.NEVER);
			case ENTITIES -> config.entityCollisionMode = unignore ? EntityCollisionMode.ALWAYS
				: EntityCollisionMode.NEVER;
			case FLUIDS -> config.fluidCollisionMode = unignore ? FluidCollisionMode.ALWAYS
				: (partial ? FluidCollisionMode.SOURCE_ONLY : FluidCollisionMode.NEVER);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String prefix = unignore ? "unignore " : "ignore ";
		return prefix + switch (target) {
			case BLOCKS -> partial ? "passable blocks" : "all blocks";
			case ENTITIES -> "all entities";
			default -> partial ? "flowing fluids" : "all fluids";
		};
	}

}
