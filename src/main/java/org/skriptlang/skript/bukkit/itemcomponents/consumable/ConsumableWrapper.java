package org.skriptlang.skript.bukkit.itemcomponents.consumable;

import ch.njol.skript.util.ItemSource;
import io.papermc.paper.datacomponent.DataComponentBuilder;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableWrapper.ConsumableBuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ConsumableWrapper extends ComponentWrapper<Consumable, ConsumableBuilder> {

	public ConsumableWrapper(ItemStack itemStack) {
		super(itemStack);
	}

	public ConsumableWrapper(ItemSource<?> itemSource) {
		super(itemSource);
	}

	public ConsumableWrapper(Consumable component) {
		super(component);
	}

	public ConsumableWrapper(ConsumableBuilder builder) {
		super(builder);
	}

	@Override
	public Valued<Consumable> getDataComponentType() {
		return DataComponentTypes.CONSUMABLE;
	}

	@Override
	protected Consumable getComponent(ItemStack itemStack) {
		Consumable consumable = itemStack.getData(DataComponentTypes.CONSUMABLE);
		if (consumable != null)
			return consumable;
		return Consumable.consumable().build();
	}

	@Override
	protected ConsumableBuilder getBuilder(ItemStack itemStack) {
		Consumable consumable = itemStack.getData(DataComponentTypes.CONSUMABLE);
		if (consumable != null)
			return new ConsumableBuilder(consumable);
		return new ConsumableBuilder();
	}

	@Override
	protected void setComponent(ItemStack itemStack, Consumable component) {
		itemStack.setData(DataComponentTypes.CONSUMABLE, component);
	}

	@Override
	protected ConsumableBuilder getBuilder(Consumable component) {
		return new ConsumableBuilder(component);
	}

	@Override
	public ConsumableWrapper clone() {
		ConsumableWrapper clone = newWrapper();
		Consumable base = getComponent();
		return clone;
	}

	@Override
	public Consumable newComponent() {
		return newBuilder().build();
	}

	@Override
	public ConsumableBuilder newBuilder() {
		return new ConsumableBuilder();
	}

	@Override
	public ConsumableWrapper newWrapper() {
		return newInstance();
	}

	public static ConsumableWrapper newInstance() {
		return new ConsumableWrapper(Consumable.consumable().build());
	}

	/**
	 * Custom builder class for {@link Consumable} that mimics {@link Consumable.Builder} allowing methods not
	 * shared across all versions to be used.
	 */
	@SuppressWarnings("NonExtendableApiUsage")
	public static class ConsumableBuilder implements DataComponentBuilder<Consumable> {

		private ItemUseAnimation animation = ItemUseAnimation.EAT;
		private List<ConsumeEffect> consumeEffects = new ArrayList<>();
		private boolean consumeParticles = true;
		private float consumeSeconds = 5;
		private Key sound = Registry.SOUNDS.getKey(Sound.ENTITY_GENERIC_EAT);

		public ConsumableBuilder() {}

		public ConsumableBuilder(Consumable consumable) {
			this.animation = consumable.animation();
			this.consumeEffects.addAll(consumable.consumeEffects());
			this.consumeParticles = consumable.hasConsumeParticles();
			this.consumeSeconds = consumable.consumeSeconds();
			this.sound = consumable.sound();
		}

		public ConsumableBuilder animation(ItemUseAnimation animation) {
			this.animation = animation;
			return this;
		}

		public ConsumableBuilder addEffect(ConsumeEffect effect) {
			consumeEffects.add(effect);
			return this;
		}

		public ConsumableBuilder addEffects(List<ConsumeEffect> effects) {
			this.consumeEffects.addAll(effects);
			return this;
		}

		public ConsumableBuilder effects(List<ConsumeEffect> effects) {
			this.consumeEffects.clear();
			this.consumeEffects.addAll(effects);
			return this;
		}

		public ConsumableBuilder hasConsumeParticles(boolean consumeParticles) {
			this.consumeParticles = consumeParticles;
			return this;
		}

		public ConsumableBuilder consumeSeconds(float seconds) {
			this.consumeSeconds = seconds;
			return this;
		}

		public ConsumableBuilder sound(Key sound) {
			this.sound = sound;
			return this;
		}

		@Override
		public Consumable build() {
			return Consumable.consumable()
				.animation(animation)
				.addEffects(consumeEffects)
				.hasConsumeParticles(consumeParticles)
				.consumeSeconds(consumeSeconds)
				.sound(sound)
				.build();
		}

	}

}
