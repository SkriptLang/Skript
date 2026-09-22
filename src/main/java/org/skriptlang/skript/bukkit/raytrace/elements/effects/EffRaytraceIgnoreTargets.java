package org.skriptlang.skript.bukkit.raytrace.elements.effects;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.raytrace.RaytraceConfig;
import org.skriptlang.skript.bukkit.raytrace.RaytraceSectionEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raytrace - Ignore Specific Targets")
@Description("""
	Tells the raytrace this section belongs to to skip specific things.
	This accepts entities, types of entity, blocks, and types of block, in any combination.
	Ignoring a type skips everything of that type, so 'ignore zombies' skips every zombie the ray meets, \
	whereas ignoring an entity or a block skips only that one.
	Can only be used within a raytrace section.
	""")
@Example("""
	set {_hit} to the results of a raytrace from player for 20 meters:
		# the ray passes straight through glass and through the player's pet
		ignore glass
		ignore {_pet}
	""")
@Example("""
	set {_hit} to the results of a raytrace from player for 20 meters:
		ignore zombies and skeletons
	""")
@Since("INSERT VERSION")
public class EffRaytraceIgnoreTargets extends Effect implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffRaytraceIgnoreTargets.class)
				.supplier(EffRaytraceIgnoreTargets::new)
				.addPattern("ignore %entities/entitydatas/blocks/itemtypes%")
				.build()
		);
	}

	private Expression<?> targets;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		targets = expressions[0];
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

		for (Object target : targets.getArray(event)) {
			if (target instanceof Entity entity) {
				config.ignoredEntities.add(entity);
			} else if (target instanceof EntityData<?> entityType) {
				config.ignoredEntityTypes.add(entityType);
			} else if (target instanceof Block block) {
				config.ignoredBlockLocations.add(block.getLocation());
			} else if (target instanceof ItemType blockType) {
				config.ignoredBlockTypes.add(blockType);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "ignore " + targets.toString(event, debug);
	}

}
