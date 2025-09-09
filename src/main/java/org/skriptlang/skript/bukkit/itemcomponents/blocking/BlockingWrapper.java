package org.skriptlang.skript.bukkit.itemcomponents.blocking;

import ch.njol.skript.util.ItemSource;
import io.papermc.paper.datacomponent.DataComponentBuilder;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.damage.DamageType;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper.BlockingBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ComponentWrapper} for getting and setting data on a {@link BlocksAttacks} component.
 */
@SuppressWarnings("UnstableApiUsage")
public class BlockingWrapper extends ComponentWrapper<BlocksAttacks, BlockingBuilder> {

	public BlockingWrapper(ItemStack itemStack) {
		super(itemStack);
	}

	public BlockingWrapper(ItemSource<?> itemSource) {
		super(itemSource);
	}

	public BlockingWrapper(BlocksAttacks component) {
		super(component);
	}

	public BlockingWrapper(BlockingBuilder builder) {
		super(builder);
	}

	@Override
	public Valued<BlocksAttacks> getDataComponentType() {
		return DataComponentTypes.BLOCKS_ATTACKS;
	}

	@Override
	protected BlocksAttacks getComponent(ItemStack itemStack) {
		BlocksAttacks component = itemStack.getData(DataComponentTypes.BLOCKS_ATTACKS);
		if (component != null)
			return component;
		return BlocksAttacks.blocksAttacks().build();
	}

	@Override
	protected BlockingBuilder getBuilder(ItemStack itemStack) {
		BlocksAttacks component = itemStack.getData(DataComponentTypes.BLOCKS_ATTACKS);
		if (component != null)
			return new BlockingBuilder(component);
		return new BlockingBuilder();
	}

	@Override
	protected void setComponent(ItemStack itemStack, BlocksAttacks component) {
		itemStack.setData(DataComponentTypes.BLOCKS_ATTACKS, component);
	}

	@Override
	protected BlockingBuilder getBuilder(BlocksAttacks component) {
		return new BlockingBuilder(component);
	}

	@Override
	public BlockingWrapper clone() {
		BlockingWrapper clone = newWrapper();
		BlocksAttacks base = getComponent();
		clone.applyComponent(new BlockingBuilder(base).build());
		return clone;
	}

	@Override
	public BlocksAttacks newComponent() {
		return new BlockingBuilder().build();
	}

	@Override
	public BlockingBuilder newBuilder() {
		return new BlockingBuilder();
	}

	@Override
	public BlockingWrapper newWrapper() {
		return newInstance();
	}

	public List<DamageReductionWrapper> getDamageReductions() {
		List<DamageReductionWrapper> wrappers = new ArrayList<>();
		getComponent().damageReductions().forEach(reduction ->
			wrappers.add(new DamageReductionWrapper(this, reduction)));
		return wrappers;
	}

	public static BlockingWrapper newInstance() {
		return new BlockingWrapper(new BlockingBuilder().build());
	}

	@SuppressWarnings("NonExtendableApiUsage")
	public static class BlockingBuilder implements DataComponentBuilder<BlocksAttacks> {

		private List<DamageReduction> damageReductions = new ArrayList<>();
		private float blockDelaySeconds = 0f;
		private float disableCooldownScale = 1f;
		private ItemDamageFunction damageFunction = ItemDamageFunction.itemDamageFunction().build();
		private @Nullable TagKey<DamageType> bypassedBy = null;
		private @Nullable Key blockSound = null;
		private @Nullable Key disableSound = null;

		public BlockingBuilder() {}

		public BlockingBuilder(BlocksAttacks component) {
			damageReductions.addAll(new ArrayList<>(component.damageReductions()));
			blockDelaySeconds = component.blockDelaySeconds();
			disableCooldownScale = component.disableCooldownScale();
			damageFunction = component.itemDamage();
			bypassedBy = component.bypassedBy();
			blockSound = component.blockSound();
			disableSound = component.disableSound();
		}

		public BlockingBuilder addDamageReduction(DamageReduction reduction) {
			damageReductions.add(reduction);
			return this;
		}

		public BlockingBuilder addDamageReductions(List<DamageReduction> reductions) {
			damageReductions.addAll(reductions);
			return this;
		}

		public BlockingBuilder damageReductions(List<DamageReduction> reductions) {
			damageReductions.clear();
			damageReductions.addAll(reductions);
			return this;
		}

		public BlockingBuilder removeReduction(DamageReduction reduction) {
			damageReductions.remove(reduction);
			return this;
		}

		public BlockingBuilder removeReductions(List<DamageReduction> reductions) {
			damageReductions.removeAll(reductions);
			return this;
		}

		public BlockingBuilder blockDelaySeconds(float blockDelaySeconds) {
			this.blockDelaySeconds = blockDelaySeconds;
			return this;
		}

		public BlockingBuilder disableCooldownScale(float disableCooldownScale) {
			this.disableCooldownScale = disableCooldownScale;
			return this;
		}

		public BlockingBuilder itemDamage(ItemDamageFunction damageFunction) {
			this.damageFunction = damageFunction;
			return this;
		}

		public BlockingBuilder bypassedBy(@Nullable TagKey<DamageType> bypassedBy) {
			this.bypassedBy = bypassedBy;
			return this;
		}

		public BlockingBuilder blockSound(@Nullable Key blockSound) {
			this.blockSound = blockSound;
			return this;
		}

		public BlockingBuilder disableSound(@Nullable Key disableSound) {
			this.disableSound = disableSound;
			return this;
		}

		@Override
		public BlocksAttacks build() {
			return BlocksAttacks.blocksAttacks()
				.damageReductions(damageReductions)
				.blockDelaySeconds(blockDelaySeconds)
				.disableCooldownScale(disableCooldownScale)
				.itemDamage(damageFunction)
				.bypassedBy(bypassedBy)
				.blockSound(blockSound)
				.disableSound(disableSound)
				.build();
		}

	}

}
