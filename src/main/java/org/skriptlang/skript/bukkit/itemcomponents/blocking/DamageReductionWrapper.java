package org.skriptlang.skript.bukkit.itemcomponents.blocking;

import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.damage.DamageType;
import org.checkerframework.checker.index.qual.Positive;
import org.jspecify.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentUtils;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class DamageReductionWrapper {

	private BlockingWrapper wrapper;
	private DamageReduction damageReduction;

	public DamageReductionWrapper(BlockingWrapper wrapper, DamageReduction damageReduction) {
		this.wrapper = wrapper;
		this.damageReduction = damageReduction;
	}

	public DamageReductionWrapper(DamageReduction damageReduction) {
		this.damageReduction = damageReduction;
	}

	public DamageReductionWrapper() {
		this.damageReduction = new Builder().buildDamageReduction();
	}

	public DamageReduction getDamageReduction() {
		return damageReduction;
	}

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

	public Builder toBuilder() {
		return new Builder(damageReduction);
	}

	public static class Builder {

		private float base = 0f;
		private float factor = 0f;
		private float horizontalBlockingAngle = 90f;
		private @Nullable RegistryKeySet<DamageType> types = null;

		public Builder() {}

		public Builder(DamageReduction damageReduction) {
			base = damageReduction.base();
			factor = damageReduction.factor();
			horizontalBlockingAngle = damageReduction.horizontalBlockingAngle();
			types = damageReduction.type();
		}

		public Builder base(float base) {
			this.base = base;
			return this;
		}

		public Builder factor(float factor) {
			this.factor = factor;
			return this;
		}

		public Builder types(@Nullable RegistryKeySet<DamageType> types) {
			this.types = types;
			return this;
		}

		public Builder types(@Nullable List<DamageType> types) {
			if (types == null || types.isEmpty()) {
				this.types = null;
			} else {
				this.types = ComponentUtils.collectionToRegistryKeySet(types, RegistryKey.DAMAGE_TYPE);
			}
			return this;
		}

		public Builder horizontalBlockingAngle(@Positive float horizontalBlockingAngle) {
			this.horizontalBlockingAngle = horizontalBlockingAngle;
			return this;
		}

		public DamageReduction buildDamageReduction() {
			return DamageReduction.damageReduction()
				.base(base)
				.factor(factor)
				.horizontalBlockingAngle(horizontalBlockingAngle)
				.type(types)
				.build();
		}

		public DamageReductionWrapper build() {
			return new DamageReductionWrapper(buildDamageReduction());
		}

	}

}
