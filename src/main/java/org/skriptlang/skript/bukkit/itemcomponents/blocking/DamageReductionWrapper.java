package org.skriptlang.skript.bukkit.itemcomponents.blocking;

import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.damage.DamageType;
import org.checkerframework.checker.index.qual.Positive;
import org.jspecify.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * Wrapper for {@link DamageReduction}.
 */
@SuppressWarnings("UnstableApiUsage")
public class DamageReductionWrapper {

	private BlockingWrapper wrapper;
	private DamageReduction damageReduction;

	/**
	 * Construct a new {@link DamageReductionWrapper} with the parent {@link BlockingWrapper} and the
	 * {@link DamageReduction} it belongs to.
	 * @param wrapper The {@link BlockingWrapper}.
	 * @param damageReduction The {@link DamageReduction}.
	 */
	public DamageReductionWrapper(BlockingWrapper wrapper, DamageReduction damageReduction) {
		this.wrapper = wrapper;
		this.damageReduction = damageReduction;
	}

	/**
	 * Construct a new {@link DamageReductionWrapper} with the {@link DamageReduction}.
	 * @param damageReduction The {@link DamageReduction}.
	 */
	public DamageReductionWrapper(DamageReduction damageReduction) {
		this.damageReduction = damageReduction;
	}

	/**
	 * Construct a new {@link DamageReductionWrapper} with a blank {@link DamageReduction}.
	 */
	public DamageReductionWrapper() {
		this.damageReduction = new Builder().buildDamageReduction();
	}

	/**
	 * @return The {@link DamageReduction} this {@link DamageReductionWrapper} is wrapping.
	 */
	public DamageReduction getDamageReduction() {
		return damageReduction;
	}

	/**
	 * Modify the current {@link DamageReduction} via {@link Builder}.
	 * <p>
	 *     If {@link #wrapper} is not null, and the {@link BlocksAttacks#damageReductions()} contains the {@link DamageReduction}
	 *     before the changes, the {@link DamageReduction} in {@link #wrapper} will be updated.
	 *     If it does not equal, {@link #wrapper} will be set to {@code null}.
	 * </p>
	 * @param consumer The consumer to change data of the {@link Builder}.
	 */
	public void modify(Consumer<Builder> consumer) {
		Builder builder = toBuilder();
		consumer.accept(builder);
		DamageReduction newDamageReduction = builder.buildDamageReduction();
		if (wrapper != null) {
			if (wrapper.getComponent().damageReductions().contains(damageReduction)) {
				wrapper.editBuilder(wrapperBuilder -> {
					wrapperBuilder.removeReduction(damageReduction);
					wrapperBuilder.addDamageReduction(newDamageReduction);
				});
			} else {
				wrapper = null;
			}
		}
		damageReduction = newDamageReduction;
	}

	/**
	 * @return The {@link Builder} with the data from {@link #damageReduction}.
	 */
	public Builder toBuilder() {
		return new Builder(damageReduction);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof DamageReductionWrapper other))
			return false;
		return damageReduction.equals(other.damageReduction);
	}

	/**
	 * Custom builder class for {@link DamageReduction}.
	 */
	public static class Builder {

		private float base = 0f;
		private float factor = 0f;
		private float horizontalBlockingAngle = 90f;
		private @Nullable RegistryKeySet<DamageType> types = null;

		/**
		 * Construct a new {@link Builder}.
		 */
		public Builder() {}

		/**
		 * Construct a new {@link Builder} and copying the data from {@code damageReduction}.
		 * @param damageReduction The {@link DamageReduction} to copy data from.
		 */
		public Builder(DamageReduction damageReduction) {
			base = damageReduction.base();
			factor = damageReduction.factor();
			horizontalBlockingAngle = damageReduction.horizontalBlockingAngle();
			types = damageReduction.type();
		}

		/**
		 * Set the base.
		 * @param base The base amount to set.
		 * @return {@code this}
		 */
		public Builder base(float base) {
			this.base = base;
			return this;
		}

		/**
		 * Set the factor.
		 * @param factor The factor amount to set.
		 * @return {@code this}
		 */
		public Builder factor(float factor) {
			this.factor = factor;
			return this;
		}

		/**
		 * Set the damage types.
		 * @param types The damage types to set.
		 * @return {@code this}
		 */
		public Builder types(@Nullable RegistryKeySet<DamageType> types) {
			this.types = types;
			return this;
		}

		/**
		 * Set the damage types.
		 * @param types The damage types to set.
		 * @return {@code this}
		 */
		public Builder types(@Nullable List<DamageType> types) {
			if (types == null || types.isEmpty()) {
				this.types = null;
			} else {
				this.types = ComponentUtils.collectionToRegistryKeySet(types, RegistryKey.DAMAGE_TYPE);
			}
			return this;
		}

		/**
		 * Set the horizontal blocking angle.
		 * @param horizontalBlockingAngle The horizontal blocking aingle to set.
		 * @return {@code this}
		 */
		public Builder horizontalBlockingAngle(@Positive float horizontalBlockingAngle) {
			this.horizontalBlockingAngle = horizontalBlockingAngle;
			return this;
		}

		/**
		 * @return The finalized {@link DamageReduction}.
		 */
		public DamageReduction buildDamageReduction() {
			return DamageReduction.damageReduction()
				.base(base)
				.factor(factor)
				.horizontalBlockingAngle(horizontalBlockingAngle)
				.type(types)
				.build();
		}

		/**
		 * @return New {@link DamageReductionWrapper} with the finalized {@link DamageReduction}.
		 */
		public DamageReductionWrapper build() {
			return new DamageReductionWrapper(buildDamageReduction());
		}

	}

}
