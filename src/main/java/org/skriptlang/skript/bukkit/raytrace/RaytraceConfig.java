package org.skriptlang.skript.bukkit.raytrace;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder;
import io.papermc.paper.raytracing.RayTraceTarget;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.condition.Conditional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The mutable configuration of a single raytrace execution.
 * @see RaytraceSectionEvent
 */
public class RaytraceConfig {

	public enum BlockCollisionMode {
		/**
		 * Ignore blocks.
		 */
		NEVER,
		/**
		 * Only collide with impassable blocks.
		 */
		IGNORE_PASSABLES,
		/**
		 * Collide with all blocks.
		 */
		ALWAYS
	}

	public enum EntityCollisionMode {
		/**
		 * Ignore entities.
		 */
		NEVER,
		/**
		 * Collide with all entities.
		 */
		ALWAYS
	}

	public double raySize = 0.0;
	public double maxDistance = SkriptConfig.maxTargetBlockDistance.value().doubleValue();

	public FluidCollisionMode fluidCollisionMode = FluidCollisionMode.ALWAYS;
	public BlockCollisionMode blockCollisionMode = BlockCollisionMode.ALWAYS;
	public EntityCollisionMode entityCollisionMode = EntityCollisionMode.ALWAYS;

	/**
	 * An entity the ray may never hit, being the entity the ray was cast from.
	 * This mirrors the behaviour of {@link org.bukkit.entity.LivingEntity#rayTraceEntities(int)}, which never
	 * returns the entity the ray originates from.
	 */
	public @Nullable Entity excludedEntity;

	public final List<Conditional<Event>> entityFilters = new ArrayList<>();
	public final List<Conditional<Event>> blockFilters = new ArrayList<>();

	/**
	 * Specific entities the ray may not hit.
	 */
	public final Set<Entity> ignoredEntities = new LinkedHashSet<>();

	/**
	 * Types of entity the ray may not hit, such as every zombie.
	 */
	public final Set<EntityData<?>> ignoredEntityTypes = new LinkedHashSet<>();

	/**
	 * The positions of specific blocks the ray may not hit.
	 * Blocks are held by position so that snapshots, delayed-change blocks and any other wrapper are treated
	 * as the block they stand for.
	 */
	public final Set<Location> ignoredBlockLocations = new LinkedHashSet<>();

	/**
	 * Types of block the ray may not hit, such as every stone block.
	 */
	public final Set<ItemType> ignoredBlockTypes = new LinkedHashSet<>();

	/**
	 * @return Whether the given entity is one this configuration was told to ignore.
	 */
	private boolean isIgnored(Entity entity) {
		if (ignoredEntities.contains(entity))
			return true;
		for (EntityData<?> entityType : ignoredEntityTypes) {
			if (entityType.isInstance(entity))
				return true;
		}
		return false;
	}

	/**
	 * @return Whether the given block is one this configuration was told to ignore.
	 */
	private boolean isIgnored(Block block) {
		if (!ignoredBlockLocations.isEmpty() && ignoredBlockLocations.contains(block.getLocation()))
			return true;
		for (ItemType blockType : ignoredBlockTypes) {
			if (blockType.isOfType(block))
				return true;
		}
		return false;
	}

	/**
	 * @return Whether this configuration allows the raytrace to hit anything at all.
	 */
	public boolean canHitAnything() {
		return blockCollisionMode != BlockCollisionMode.NEVER || entityCollisionMode != EntityCollisionMode.NEVER;
	}

	/**
	 * Builds a consumer applying this configuration to a raytrace builder.
	 * @see org.bukkit.World#rayTrace(Consumer)
	 *
	 * @param start The location the ray starts at.
	 * @param direction The direction the ray travels in.
	 * @param maxDistance The maximum distance the ray travels.
	 */
	public Consumer<PositionedRayTraceConfigurationBuilder> asBuilder(Location start, Vector direction, double maxDistance) {
		return builder -> {
			// set simple info
			builder.start(start)
				.direction(direction)
				.maxDistance(maxDistance)
				.raySize(raySize)
				.ignorePassableBlocks(blockCollisionMode == BlockCollisionMode.IGNORE_PASSABLES)
				.fluidCollisionMode(fluidCollisionMode);

			// set targets
			if (blockCollisionMode != BlockCollisionMode.NEVER) {
				if (entityCollisionMode != EntityCollisionMode.NEVER) {
					builder.targets(RayTraceTarget.BLOCK, RayTraceTarget.ENTITY);
				} else {
					builder.targets(RayTraceTarget.BLOCK);
				}
			} else if (entityCollisionMode != EntityCollisionMode.NEVER) {
				builder.targets(RayTraceTarget.ENTITY);
			}

			// entity filter
			Conditional<Event> entityIgnoreIf = entityFilters.isEmpty() ? null
				: Conditional.compound(Conditional.Operator.OR, entityFilters);

			if (excludedEntity != null || entityIgnoreIf != null
				|| !ignoredEntities.isEmpty() || !ignoredEntityTypes.isEmpty()) {

				builder.entityFilter(entity -> {
					// the entity the ray was cast from can never be hit
					if (entity.equals(excludedEntity))
						return false;
					if (isIgnored(entity))
						return false;
					return entityIgnoreIf == null || !entityIgnoreIf.evaluate(new RaytraceEntityEvent(entity)).isTrue();
				});
			}
			// block filter
			Conditional<Event> blockIgnoreIf = blockFilters.isEmpty() ? null
				: Conditional.compound(Conditional.Operator.OR, blockFilters);

			if (blockIgnoreIf != null || !ignoredBlockLocations.isEmpty() || !ignoredBlockTypes.isEmpty()) {
				builder.blockFilter(block -> {
					if (isIgnored(block))
						return false;
					return blockIgnoreIf == null || !blockIgnoreIf.evaluate(new RaytraceBlockEvent(block)).isTrue();
				});
			}
		};
	}

	@Override
	public String toString() {
		return "RaytraceConfig{" +
			"raySize=" + raySize +
			", fluidCollisionMode=" + fluidCollisionMode +
			", blockCollisionMode=" + blockCollisionMode +
			", entityCollisionMode=" + entityCollisionMode +
			", excludedEntity=" + excludedEntity +
			", entityFilters=" + entityFilters +
			", blockFilters=" + blockFilters +
			'}';
	}

}
