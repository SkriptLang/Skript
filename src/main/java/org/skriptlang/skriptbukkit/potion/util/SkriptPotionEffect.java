/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skriptbukkit.potion.util;

import ch.njol.skript.util.Timespan;
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
		builder.append("potion effect of ");
		builder.append(PotionUtils.toString(potionEffectType));
		builder.append(" ");
		builder.append(amplifier + 1);
		if (!particles)
			builder.append(" without particles");
		builder.append(" for ").append(Timespan.fromTicks_i(duration));
		return builder.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SkriptPotionEffect))
			return false;
		if (this == other)
			return true;
		SkriptPotionEffect otherPotion = (SkriptPotionEffect) other;
		return this.potionEffectType.equals(otherPotion.potionEffectType)
			&& this.duration == otherPotion.duration
			&& this.amplifier == otherPotion.amplifier
			&& this.ambient == otherPotion.ambient
			&& this.particles == otherPotion.particles
			&& this.icon == otherPotion.icon;
	}

}
