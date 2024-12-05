package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;

public class LootContextWrapper {

	private final Location location;
	private @Nullable HumanEntity killer;
	private @Nullable Entity entity;
	private float luck;

	public LootContextWrapper(Location location) {
		this.location = location;
	}

	public @Nullable LootContext getContext() {
		LootContext.Builder builder = new LootContext.Builder(location);
		return builder
			.killer(killer)
			.lootedEntity(entity)
			.luck(luck)
			.build();
	}

	public void setKiller(@Nullable HumanEntity killer) {
		this.killer = killer;
	}

	public void setEntity(@Nullable Entity entity) {
		this.entity = entity;
	}

	public void setLuck(float luck) {
		this.luck = luck;
	}

	public Location getLocation() {
		return location;
	}

	public @Nullable HumanEntity getKiller() {
		return killer;
	}

	public @Nullable Entity getEntity() {
		return entity;
	}

	public float getLuck() {
		return luck;
	}

}
