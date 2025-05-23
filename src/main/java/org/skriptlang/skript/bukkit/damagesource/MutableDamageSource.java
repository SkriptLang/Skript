package org.skriptlang.skript.bukkit.damagesource;

import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mutable {@link DamageSource} for continuous changing of data.
 * <p>
 *     {@link #damageType} should always be set.
 *     Must set the {@link #directEntity} when setting the {@link #causingEntity}.
 *     Needs to be converted into a normal {@link DamageSource} via {@link #asBukkitSource()} before use.
 * </p>
 */
@SuppressWarnings("UnstableApiUsage")
public class MutableDamageSource implements DamageSource {

	private DamageType damageType = DamageType.GENERIC;
	private @Nullable Entity causingEntity = null;
	private @Nullable Entity directEntity = null;
	private @Nullable Location damageLocation = null;
	private float foodExhaustion = 0f;
	private boolean scalesWithDifficulty = false;

	public MutableDamageSource() {}

	public MutableDamageSource(DamageSource damageSource) {
		this.damageType = damageSource.getDamageType();
		this.causingEntity = damageSource.getCausingEntity();
		this.directEntity = damageSource.getDirectEntity();
		this.damageLocation = damageSource.getDamageLocation() == null ?
			null : damageSource.getDamageLocation().clone();
		this.foodExhaustion = damageSource.getFoodExhaustion();
		this.scalesWithDifficulty = damageSource.scalesWithDifficulty();
	}

	@Override
	public @NotNull DamageType getDamageType() {
		return damageType;
	}

	@Override
	public @Nullable Entity getCausingEntity() {
		return causingEntity;
	}

	@Override
	public @Nullable Entity getDirectEntity() {
		return directEntity;
	}

	@Override
	public @Nullable Location getDamageLocation() {
		return damageLocation == null ? null : damageLocation.clone();
	}

	@Override
	public @Nullable Location getSourceLocation() {
		if (damageLocation != null)
			return damageLocation.clone();
		if (causingEntity != null)
			return causingEntity.getLocation().clone();
		return null;
	}

	@Override
	public boolean isIndirect() {
		return causingEntity != null;
	}

	@Override
	public float getFoodExhaustion() {
		return foodExhaustion;
	}

	@Override
	public boolean scalesWithDifficulty() {
		return scalesWithDifficulty;
	}

	public MutableDamageSource setDamageType(DamageType damageType) {
		this.damageType = damageType;
		return this;
	}

	public MutableDamageSource setCausingEntity(@Nullable Entity causingEntity) {
		this.causingEntity = causingEntity;
		return this;
	}

	public MutableDamageSource setDirectEntity(@Nullable Entity directEntity) {
		this.directEntity = directEntity;
		return this;
	}

	public MutableDamageSource setDamageLocation(@Nullable Location damageLocation) {
		this.damageLocation = damageLocation;
		return this;
	}

	/**
	 * {@link MutableDamageSource} is unable to be used with the only current method that accepts a {@link DamageSource},
	 * {@link org.bukkit.entity.Damageable#damage(double, DamageSource)} as it can not be cast to a 'CraftDamageSource'.
	 * Must grab a finalized {@link DamageSource} with this method.
	 * @return {@link DamageSource}.
	 */
	public DamageSource asBukkitSource() {
		DamageSource.Builder builder = DamageSource.builder(damageType);
		if (damageLocation != null)
			builder = builder.withDamageLocation(damageLocation.clone());
		if (causingEntity != null)
			builder = builder.withCausingEntity(causingEntity);
		if (directEntity != null)
			builder = builder.withDirectEntity(directEntity);
		return builder.build();
	}

	/**
	 * Get a copy of a {@link DamageSource}.
	 * @param damageSource The {@link DamageSource} to copy.
	 * @return The copied {@link DamageSource}
	 */
	public static DamageSource copy(DamageSource damageSource) {
		return new MutableDamageSource(damageSource).asBukkitSource();
	}

}
