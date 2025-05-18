package org.skriptlang.skript.bukkit.damagesource;

import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper for continuous changing of a {@link DamageSource}.
 */
@SuppressWarnings("ALL")
public class DamageSourceWrapper implements DamageSource {

	private DamageType damageType = DamageType.GENERIC;
	private @Nullable Entity causingEntity = null;
	private @Nullable Entity directEntity = null;
	private @Nullable Location damageLocation = null;
	private @Nullable Location sourceLocation = null;
	private boolean indirect = false;
	private float foodExhaustion = 0f;
	private boolean scalesWithDifficulty = false;

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
		return damageLocation;
	}

	@Override
	public @Nullable Location getSourceLocation() {
		return sourceLocation;
	}

	@Override
	public boolean isIndirect() {
		return indirect;
	}

	@Override
	public float getFoodExhaustion() {
		return foodExhaustion;
	}

	@Override
	public boolean scalesWithDifficulty() {
		return scalesWithDifficulty;
	}

	public DamageSourceWrapper setDamageType(DamageType damageType) {
		this.damageType = damageType;
		return this;
	}

	public DamageSourceWrapper setCausingEntity(@Nullable Entity causingEntity) {
		this.causingEntity = causingEntity;
		return this;
	}

	public DamageSourceWrapper setDirectEntity(@Nullable Entity directEntity) {
		this.directEntity = directEntity;
		return this;
	}

	public DamageSourceWrapper setDamageLocation(@Nullable Location damageLocation) {
		this.damageLocation = damageLocation;
		return this;
	}

	public DamageSourceWrapper setSourceLocation(@Nullable Location sourceLocation) {
		this.sourceLocation = sourceLocation;
		return this;
	}

	public DamageSourceWrapper setIndirect(boolean indirect) {
		this.indirect = indirect;
		return this;
	}

	public DamageSourceWrapper setFoodExhaustion(float foodExhaustion) {
		this.foodExhaustion = foodExhaustion;
		return this;
	}

	public DamageSourceWrapper setScalesWithDifficulty(boolean scalesWithDifficulty) {
		this.scalesWithDifficulty = scalesWithDifficulty;
		return this;
	}

	/**
	 * {@link DamageSourceWrapper} is unable to be used with the only current method that accepts a {@link DamageSource},
	 * {@link org.bukkit.entity.Damageable#damage(double, DamageSource)} as it can not be casted to a 'CraftDamageSource'.
	 * Must grab a finalize {@link DamageSource} with this method.
	 * @return {@link DamageSource}.
	 */
	public DamageSource build() {
		return DamageSource.builder(damageType)
			.withDamageLocation(damageLocation)
			.withCausingEntity(causingEntity)
			.withDirectEntity(directEntity)
			.build();
	}

	public static DamageSource clone(DamageSource damageSource) {
		return new DamageSourceWrapper()
			.setDamageType(damageSource.getDamageType())
			.setCausingEntity(damageSource.getCausingEntity())
			.setDirectEntity(damageSource.getDirectEntity())
			.setDamageLocation(damageSource.getSourceLocation());
	}

	public static @Nullable DamageSource clone(Object object) {
		if (object instanceof DamageSource damageSource)
			return clone(damageSource);
		return null;
	}

}
