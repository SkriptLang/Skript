package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayDeque;
import java.util.Deque;

public final class PotionUtils {

	/**
	 * 30 seconds is the default length for the /effect command
	 * See <a href="https://minecraft.wiki/w/Commands/effect">https://minecraft.wiki/w/Commands/effect</a>
	 */
	public static final int DEFAULT_DURATION_TICKS = 600;
	/**
	 * A string representation of a {@link Timespan} of {@link #DEFAULT_DURATION_TICKS}.
	 */
	public static final String DEFAULT_DURATION_STRING = new Timespan(TimePeriod.TICK, DEFAULT_DURATION_TICKS).toString();

	/**
	 * A utility method to obtain the hidden effects of a potion effect.
	 * @param effect The effect to obtain hidden effects from.
	 * @return A deque of the hidden effects of {@code effect} ordered from most hidden to least hidden.
	 */
	public static Deque<PotionEffect> getHiddenEffects(PotionEffect effect) {
		Deque<PotionEffect> hiddenEffects = new ArrayDeque<>();

		// build hidden effects chain to reapply
		PotionEffect hiddenEffect = effect.getHiddenPotionEffect();
		while (hiddenEffect != null) {
			hiddenEffects.push(hiddenEffect);
			hiddenEffect = hiddenEffect.getHiddenPotionEffect();
		}

		return hiddenEffects;
	}

}
