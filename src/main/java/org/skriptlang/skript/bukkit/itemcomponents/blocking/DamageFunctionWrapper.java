package org.skriptlang.skript.bukkit.itemcomponents.blocking;

import io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction;

import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class DamageFunctionWrapper {

	private BlockingWrapper wrapper;
	private ItemDamageFunction damageFunction;

	public DamageFunctionWrapper(BlockingWrapper wrapper, ItemDamageFunction damageFunction) {
		this.wrapper = wrapper;
		this.damageFunction = damageFunction;
	}

	public DamageFunctionWrapper(ItemDamageFunction damageFunction) {
		this.damageFunction = damageFunction;
	}

	public DamageFunctionWrapper() {
		this.damageFunction = ItemDamageFunction.itemDamageFunction().build();
	}

	public ItemDamageFunction getDamageFunction() {
		return damageFunction;
	}

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

	public Builder toBuilder() {
		return new Builder(damageFunction);
	}

	public static class Builder {

		private float base = 0f;
		private float factor = 1f;
		private float threshold = 1f;

		public Builder() {}

		public Builder(ItemDamageFunction damageFunction) {
			base = damageFunction.base();
			factor = damageFunction.factor();
			threshold = damageFunction.threshold();
		}

		public Builder base(float base) {
			this.base = base;
			return this;
		}

		public Builder factor(float factor) {
			this.factor = factor;
			return this;
		}

		public Builder threshold(float threshold) {
			this.threshold = threshold;
			return this;
		}

		public ItemDamageFunction buildFunction() {
			return ItemDamageFunction.itemDamageFunction()
				.base(base)
				.factor(factor)
				.threshold(threshold)
				.build();
		}

		public DamageFunctionWrapper build() {
			return new DamageFunctionWrapper(buildFunction());
		}

	}

}
