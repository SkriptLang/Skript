package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.StreamCorruptedException;

public class SkriptPotionEffect implements Cloneable, YggdrasilExtendedSerializable {

	private PotionEffectType potionEffectType;
	private int duration = PotionUtils.DEFAULT_DURATION_TICKS;
	private int amplifier = 0;
	private boolean ambient = false;
	private boolean particles = true;
	private boolean icon = true;

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

	public PotionEffectType potionEffectType() {
		return potionEffectType;
	}

	public SkriptPotionEffect potionEffectType(PotionEffectType potionEffectType) {
		this.potionEffectType = potionEffectType;
		return this;
	}

	public boolean infinite() {
		return duration == PotionUtils.INFINITE_DURATION;
	}

	// TODO docs note: making not infinite resets duration
	public SkriptPotionEffect infinite(boolean infinite) {
		return duration(infinite ? PotionUtils.INFINITE_DURATION : PotionUtils.DEFAULT_DURATION_TICKS);
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
		builder.append(Classes.toString(potionEffectType));
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
