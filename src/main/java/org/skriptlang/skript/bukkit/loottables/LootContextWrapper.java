package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper for a LootContext.Builder to allow easier creation of LootContexts.
 */
public class LootContextWrapper {

	private @NotNull Location location;
	private transient @Nullable LootContext cachedLootContext;
	private @Nullable Player killer;
	private @Nullable Entity entity;
	private float luck;

	/**
	 * Creates a new LootContextWrapper at the given location.
	 * @param location the location of the LootContext.
	 */
	public LootContextWrapper(@NotNull Location location) {
		this.location = location;
	}

	/**
	 * Gets the LootContext from the wrapper.
	 * @return the LootContext.
	 */
	public LootContext getContext() {
		if (cachedLootContext == null)
			cachedLootContext = new LootContext.Builder(location)
				.killer(killer)
				.lootedEntity(entity)
				.luck(luck)
				.build();

		return cachedLootContext;
	}

	/**
	 * Sets the location of the LootContext.
	 * @param location the location.
	 */
	public void setLocation(@NotNull Location location) {
		this.location = location;
		cachedLootContext = null;
	}

	/**
	 * Sets the killer of the LootContext.
	 * @param killer the killer.
	 */
	public void setKiller(@Nullable Player killer) {
		this.killer = killer;
		cachedLootContext = null;
	}

	/**
	 * Sets the entity of the LootContext.
	 * @param entity the entity.
	 */
	public void setEntity(@Nullable Entity entity) {
		this.entity = entity;
		cachedLootContext = null;
	}

	/**
	 * Sets the luck of the LootContext.
	 * @param luck the luck value.
	 */
	public void setLuck(float luck) {
		this.luck = luck;
		cachedLootContext = null;
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
	public @Nullable Player getKiller() {
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
