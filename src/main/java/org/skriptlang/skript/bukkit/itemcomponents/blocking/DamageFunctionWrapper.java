package org.skriptlang.skript.bukkit.itemcomponents.blocking;

import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction;

import java.util.function.Consumer;

/**
 * Wrapper for {@link ItemDamageFunction}.
 */
@SuppressWarnings("UnstableApiUsage")
public class DamageFunctionWrapper {

	private BlockingWrapper wrapper;
	private ItemDamageFunction damageFunction;

	/**
	 * Construct a new {@link DamageFunctionWrapper} with the parent {@link BlockingWrapper} and the
	 * {@link ItemDamageFunction} it belongs to.
	 * @param wrapper The {@link BlockingWrapper}.
	 * @param damageFunction The {@link ItemDamageFunction}.
	 */
	public DamageFunctionWrapper(BlockingWrapper wrapper, ItemDamageFunction damageFunction) {
		this.wrapper = wrapper;
		this.damageFunction = damageFunction;
	}

	/**
	 * Construct a new {@link DamageFunctionWrapper} with the {@link ItemDamageFunction}.
	 * @param damageFunction The {@link ItemDamageFunction}.
	 */
	public DamageFunctionWrapper(ItemDamageFunction damageFunction) {
		this.damageFunction = damageFunction;
	}

	/**
	 * Construct a new {@link DamageFunctionWrapper} with a blank {@link ItemDamageFunction}.
	 */
	public DamageFunctionWrapper() {
		this.damageFunction = ItemDamageFunction.itemDamageFunction().build();
	}

	/**
	 * @return The {@link ItemDamageFunction} this {@link DamageFunctionWrapper} is wrapping.
	 */
	public ItemDamageFunction getDamageFunction() {
		return damageFunction;
	}

	/**
	 * Modify the current {@link ItemDamageFunction} via {@link Builder}.
	 * <p>
	 *     If {@link #wrapper} is not null, and the {@link BlocksAttacks#itemDamage()} equals the {@link ItemDamageFunction}
	 *     before the changes, the {@link ItemDamageFunction} in {@link #wrapper} will be updated.
	 *     If it does not equal, {@link #wrapper} will be set to {@code null}.
	 * </p>
	 * @param consumer The consumer to change data of the {@link Builder}.
	 */
	public void modify(Consumer<Builder> consumer) {
		Builder builder = toBuilder();
		consumer.accept(builder);
		ItemDamageFunction newFunction = builder.buildFunction();
		if (wrapper != null) {
			if (wrapper.getComponent().itemDamage().equals(damageFunction)) {
				wrapper.editBuilder(wrapperBuilder -> wrapperBuilder.itemDamage(newFunction));
			} else {
				wrapper = null;
			}
		}
		damageFunction = newFunction;
	}

	/**
	 * @return The {@link Builder} with the data from {@link #damageFunction}.
	 */
	public Builder toBuilder() {
		return new Builder(damageFunction);
	}

	/**
	 * Custom builder class for {@link ItemDamageFunction}.
	 */
	public static class Builder {

		private float base = 0f;
		private float factor = 1f;
		private float threshold = 1f;

		/**
		 * Construct a new {@link Builder}.
		 */
		public Builder() {}

		/**
		 * Construct a new {@link Builder} and copying the data from {@code damageFunction}.
		 * @param damageFunction The {@link ItemDamageFunction} to copy data from.
		 */
		public Builder(ItemDamageFunction damageFunction) {
			base = damageFunction.base();
			factor = damageFunction.factor();
			threshold = damageFunction.threshold();
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
		 * Set the threshold.
		 * @param threshold The threshold amount to set.
		 * @return {@code this}
		 */
		public Builder threshold(float threshold) {
			this.threshold = threshold;
			return this;
		}

		/**
		 * @return The finalized {@link ItemDamageFunction}.
		 */
		public ItemDamageFunction buildFunction() {
			return ItemDamageFunction.itemDamageFunction()
				.base(base)
				.factor(factor)
				.threshold(threshold)
				.build();
		}

		/**
		 * @return New {@link DamageFunctionWrapper} with the finalized {@link ItemDamageFunction}.
		 */
		public DamageFunctionWrapper build() {
			return new DamageFunctionWrapper(buildFunction());
		}

	}

}
