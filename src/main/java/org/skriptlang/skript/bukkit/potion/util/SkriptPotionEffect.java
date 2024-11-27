package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SkriptPotionEffect {

	private PotionEffectType potionEffectType;
	private int duration = PotionUtils.DEFAULT_DURATION_TICKS;
	private int amplifier = 0;
	private boolean ambient = false;
	private boolean particles = true;
	private boolean icon = true;

	public SkriptPotionEffect(PotionEffectType potionEffectType) {
		this.potionEffectType = potionEffectType;
	}

	/**
	 * Creates a SkriptPotionEffect from a Bukkit PotionEffect
	 */
	public SkriptPotionEffect(PotionEffect potionEffect) {
		this.potionEffectType = potionEffect.getType();
		this.duration = potionEffect.getDuration();
		this.amplifier = potionEffect.getAmplifier();
		this.ambient = potionEffect.isAmbient();
		this.particles = potionEffect.hasParticles();
		this.icon = potionEffect.hasIcon();
	}

	public PotionEffectType potionEffectType() {
		return potionEffectType;
	}

	public SkriptPotionEffect potionEffectType(PotionEffectType potionEffectType) {
		this.potionEffectType = potionEffectType;
		return this;
	}

	// TODO should document to set duration to PotionUtils.INFINITE_DURATION
	public boolean infinite() {
		return duration == PotionUtils.INFINITE_DURATION;
	}

	public int duration() {
		return duration;
	}

	public SkriptPotionEffect duration(int duration) {
		this.duration = duration;
		return this;
	}

	public int amplifier() {
		return amplifier;
	}

	public SkriptPotionEffect amplifier(int amplifier) {
		this.amplifier = amplifier;
		return this;
	}

	public boolean ambient() {
		return ambient;
	}

	public SkriptPotionEffect ambient(boolean ambient) {
		this.ambient = ambient;
		return this;
	}

	public boolean particles() {
		return particles;
	}

	public SkriptPotionEffect particles(boolean particles) {
		this.particles = particles;
		return this;
	}

	public boolean icon() {
		return icon;
	}

	public SkriptPotionEffect icon(boolean icon) {
		this.icon = icon;
		return this;
	}

	public PotionEffect toPotionEffect() {
		return new PotionEffect(potionEffectType, duration, amplifier, ambient, particles, icon);
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int flags) {
		StringBuilder builder = new StringBuilder();
		if (ambient)
			builder.append("ambient ");
		boolean infinite = infinite();
		if (infinite)
			builder.append("infinite ");
		builder.append("potion effect of ");
		builder.append(PotionUtils.toString(potionEffectType));
		builder.append(" ");
		builder.append(amplifier + 1);
		if (!particles)
			builder.append(" without particles");
		if (!icon)
			builder.append(" without an icon");
		if (!infinite)
			builder.append(" for ").append(new Timespan(TimePeriod.TICK, duration));
		return builder.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SkriptPotionEffect otherPotion))
			return false;
		if (this == other)
			return true;
		return this.potionEffectType.equals(otherPotion.potionEffectType)
				&& this.duration == otherPotion.duration
				&& this.amplifier == otherPotion.amplifier
				&& this.ambient == otherPotion.ambient
				&& this.particles == otherPotion.particles
				&& this.icon == otherPotion.icon;
	}

	/**
	 * A property of a potion effect.
	 * This includes properties such as a potion's duration, amplifier, and effect.
	 */
	public enum Property {
		AMBIENT,
		AMPLIFIER,
		DURATION,
		EFFECT,
		ICON,
		INFINITE,
		PARTICLES;

		private final String displayName;

		Property() {
			displayName = name().toLowerCase();
		}

		public String displayName() {
			return displayName;
		}
	}

}
