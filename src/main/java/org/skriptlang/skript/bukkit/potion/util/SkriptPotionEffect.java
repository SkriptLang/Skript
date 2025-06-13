package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.StreamCorruptedException;

/**
 * A wrapper class for passing around a modifiable {@link PotionEffect}.
 */
public class SkriptPotionEffect implements Cloneable, YggdrasilExtendedSerializable {

	private PotionEffectType potionEffectType;
	private int duration = PotionUtils.DEFAULT_DURATION_TICKS;
	private int amplifier = 0;
	private boolean ambient = false;
	private boolean particles = true;
	private boolean icon = true;

	private PotionEffect lastEffect;

	/**
	 * Internal usage only for serialization.
	 */
	@ApiStatus.Internal
	public SkriptPotionEffect() { }

	/**
	 * @return A potion effect with {@link #potionEffectType()} as <code>potionEffectType</code>.
	 * Other properties hold their default values.
	 * @see #fromBukkitEffect(PotionEffect)
	 */
	public static SkriptPotionEffect fromType(PotionEffectType potionEffectType) {
		return new SkriptPotionEffect()
				.potionEffectType(potionEffectType);
	}

	/**
	 * @return A potion effect whose properties are set from <code>potionEffect</code>.
	 * @see #fromType(PotionEffectType)
	 */
	public static SkriptPotionEffect fromBukkitEffect(PotionEffect potionEffect) {
		return fromType(potionEffect.getType())
			.duration(potionEffect.getDuration())
			.amplifier(potionEffect.getAmplifier())
			.ambient(potionEffect.isAmbient())
			.particles(potionEffect.hasParticles())
			.icon(potionEffect.hasIcon());
	}

	/**
	 * @return The type of potion effect.
	 * @see PotionEffect#getType()
	 */
	public PotionEffectType potionEffectType() {
		return potionEffectType;
	}

	/**
	 * Updates the type of this potion effect.
	 * @param potionEffectType The new type of this potion effect.
	 * @return This potion effect.
	 */
	@Contract("_ -> this")
	public SkriptPotionEffect potionEffectType(PotionEffectType potionEffectType) {
		lastEffect = null;
		this.potionEffectType = potionEffectType;
		return this;
	}

	/**
	 * @return Whether this potion effect is infinite.
	 * @see PotionEffect#isInfinite()
	 */
	public boolean infinite() {
		return duration == PotionEffect.INFINITE_DURATION;
	}

	/**
	 * Updates whether this potion effect is infinite.
	 * This is a helper method that simply overrides {@link #duration()} with the correct value.
	 * @param infinite Whether this potion effect should be infinite.
	 * @return This potion effect.
	 */
	@Contract("_ -> this")
	public SkriptPotionEffect infinite(boolean infinite) {
		return duration(infinite ? PotionEffect.INFINITE_DURATION : PotionUtils.DEFAULT_DURATION_TICKS);
	}

	/**
	 * @return The duration of this potion effect.
	 * Will be {@link PotionEffect#INFINITE_DURATION} if this effect is {@link #infinite()}.
	 * @see PotionEffect#getDuration()
	 */
	public int duration() {
		return duration;
	}

	/**
	 * Updates the duration of this potion effect.
	 * @param duration The new duration of this potion effect.
	 * @return This potion effect.
	 */
	@Contract("_ -> this")
	public SkriptPotionEffect duration(int duration) {
		lastEffect = null;
		this.duration = duration;
		return this;
	}

	/**
	 * @return The amplifier of this potion effect.
	 * @see PotionEffect#getAmplifier()
	 */
	public int amplifier() {
		return amplifier;
	}

	/**
	 * Updates the amplifier of this potion effect.
	 * @param amplifier The new amplifier of this potion effect.
	 * @return This potion effect.
	 */
	@Contract("_ -> this")
	public SkriptPotionEffect amplifier(int amplifier) {
		this.amplifier = amplifier;
		return this;
	}

	/**
	 * @return Whether this potion effect is ambient.
	 * @see PotionEffect#isAmbient()
	 */
	public boolean ambient() {
		return ambient;
	}

	/**
	 * Updates whether this potion effect is ambient.
	 * @param ambient Whether this potion effect should be ambient.
	 * @return This potion effect.
	 */
	@Contract("_ -> this")
	public SkriptPotionEffect ambient(boolean ambient) {
		lastEffect = null;
		this.ambient = ambient;
		return this;
	}

	/**
	 * @return Whether this potion effect has particles.
	 * @see PotionEffect#hasParticles()
	 */
	public boolean particles() {
		return particles;
	}

	/**
	 * Updates whether this potion effect has particles.
	 * @param particles Whether this potion effect should have particles.
	 * @return This potion effect.
	 */
	@Contract("_ -> this")
	public SkriptPotionEffect particles(boolean particles) {
		lastEffect = null;
		this.particles = particles;
		return this;
	}

	/**
	 * @return Whether this potion effect has an icon.
	 * @see PotionEffect#hasIcon()
	 */
	public boolean icon() {
		return icon;
	}

	/**
	 * Updates whether this potion effect has an icon.
	 * @param icon Whether this potion effect should have an icon.
	 * @return This potion effect.
	 */
	@Contract("_ -> this")
	public SkriptPotionEffect icon(boolean icon) {
		lastEffect = null;
		this.icon = icon;
		return this;
	}

	/**
	 * Constructs a Bukkit {@link PotionEffect} from this potion effect.
	 * @return A Bukkit PotionEffect representing the values of this potion effect.
	 * Note that the returned value may be the same across multiple calls,
	 *  assuming that this potion effect's values have not changed.
	 */
	public PotionEffect toPotionEffect() {
		if (lastEffect == null) {
			lastEffect = new PotionEffect(potionEffectType, duration, amplifier, ambient, particles, icon);
		}
		return lastEffect;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	/**
	 * @param flags Currently unused.
	 * @return A human-readable string representation of this potion effect.
	 * @see #toString()
	 */
	public String toString(int flags) {
		StringBuilder builder = new StringBuilder();
		if (ambient) {
			builder.append("ambient ");
		}
		boolean infinite = infinite();
		if (infinite) {
			builder.append("infinite ");
		}
		builder.append("potion effect of ");
		builder.append(Classes.toString(potionEffectType));
		builder.append(" ");
		builder.append(amplifier + 1);
		if (!particles) {
			builder.append(" without particles");
		}
		if (!icon) {
			builder.append(" without an icon");
		}
		if (!infinite) {
			builder.append(" for ").append(new Timespan(TimePeriod.TICK, duration));
		}
		return builder.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SkriptPotionEffect otherPotion)) {
			return false;
		}
		return this.potionEffectType.equals(otherPotion.potionEffectType)
				&& this.duration == otherPotion.duration
				&& this.amplifier == otherPotion.amplifier
				&& this.ambient == otherPotion.ambient
				&& this.particles == otherPotion.particles
				&& this.icon == otherPotion.icon;
	}

	/*
	 * YggdrasilExtendedSerializable
	 */

	@Override
	public Fields serialize() {
		Fields fields = new Fields();
		fields.putObject("type", this.potionEffectType());
		fields.putPrimitive("duration", this.duration());
		fields.putPrimitive("amplifier", this.amplifier());
		fields.putPrimitive("ambient", this.ambient());
		fields.putPrimitive("particles", this.particles());
		fields.putPrimitive("icon", this.icon());
		return fields;
	}

	@Override
	public void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
		potionEffectType(fields.getObject("type", PotionEffectType.class));
		duration(fields.getPrimitive("duration", int.class));
		amplifier(fields.getPrimitive("amplifier", int.class));
		ambient(fields.getPrimitive("ambient", boolean.class));
		particles(fields.getPrimitive("particles", boolean.class));
		icon(fields.getPrimitive("icon", boolean.class));
	}

	@Override
	public SkriptPotionEffect clone() {
		try {
			return (SkriptPotionEffect) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}

}
