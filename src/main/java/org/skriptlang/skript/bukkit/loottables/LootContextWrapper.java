package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper for a LootContext.Builder to allow easier creation of LootContexts.
 */
public class LootContextWrapper extends LootContext.Builder {

	private final Location location;
	private @Nullable HumanEntity killer;
	private @Nullable Entity entity;
	private float luck;

	/**
	 * Creates a new LootContextWrapper at the given location.
	 * @param location the location of the LootContext.
	 */
	public LootContextWrapper(@NotNull Location location) {
        super(location);
        this.location = location;
	}

	/**
	 * Gets the LootContext from the wrapper.
	 * @return the LootContext.
	 */
	public @Nullable LootContext getContext() {
		return super
			.killer(killer)
			.lootedEntity(entity)
			.luck(luck)
			.build();
	}

	/**
	 * Sets the killer of the LootContext.
	 * @param killer the killer.
	 */
	public void setKiller(@Nullable HumanEntity killer) {
		this.killer = killer;
	}

	/**
	 * Sets the entity of the LootContext.
	 * @param entity the entity.
	 */
	public void setEntity(@Nullable Entity entity) {
		this.entity = entity;
	}

	/**
	 * Sets the luck of the LootContext.
	 * @param luck the luck value.
	 */
	public void setLuck(float luck) {
		this.luck = luck;
	}

	/**
	 * Gets the location of the LootContext.
	 * @return the location.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Gets the killer of the LootContext.
	 * @return the killer.
	 */
	public @Nullable HumanEntity getKiller() {
		return killer;
	}

	/**
	 * Gets the entity of the LootContext.
	 * @return the entity.
	 */
	public @Nullable Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the luck of the LootContext.
	 * @return the luck value.
	 */
	public float getLuck() {
		return luck;
	}

}
