package org.skriptlang.skript.bukkit.itemcomponents.consumable;

import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect.ApplyStatusEffects;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect.ClearAllStatusEffects;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect.PlaySound;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect.RemoveStatusEffects;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect.TeleportRandomly;

/**
 * Types of {@link ConsumeEffect}s.
 */
@SuppressWarnings("UnstableApiUsage")
public enum ConsumeEffectType {

	APPLY_STATUS_EFFECTS(ApplyStatusEffects.class),
	CLEAR_ALL_STATUS_EFFECTS(ClearAllStatusEffects.class),
	PLAY_SOUND(PlaySound.class),
	REMOVE_STATUS_EFFECTS(RemoveStatusEffects.class),
	TELEPORT_RANDOMLY(TeleportRandomly.class);

	private final Class<? extends ConsumeEffect> effectClass;

	ConsumeEffectType(Class<? extends ConsumeEffect> effectClass) {
		this.effectClass = effectClass;
	}

	/**
	 * @return The {@link ConsumeEffect} class for this {@link ConsumeEffectType}.
	 */
	public Class<? extends ConsumeEffect> getEffectClass() {
		return effectClass;
	}

}
