package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffects;

import java.io.StreamCorruptedException;
import java.util.Deque;
import java.util.StringJoiner;

/**
 * A wrapper class for passing around a modifiable {@link PotionEffect}.
 */
public class SkriptPotionEffect implements Cloneable, YggdrasilExtendedSerializable {

	private PotionEffectType potionEffectType;
	private Integer duration = null;
	private Integer amplifier = null;
	private Boolean ambient = null;
	private Boolean particles = null;
	private Boolean icon = null;

	/**
	 * Last effect built by {@link #asBukkitPotionEffect()}.
	 */
	private @Nullable PotionEffect lastEffect;

	/**
	 * Sources for where this effect was created from.
	 * Modifying this effect will update the effect on any sources.
	 */
	private @Nullable LivingEntity entitySource;
	private @Nullable ItemType itemSource;

	/**
	 * Internal usage only for serialization.
	 */
	@ApiStatus.Internal
	public SkriptPotionEffect() { }

	/**
	 * Constructs a SkriptPotionEffect from a Bukkit PotionEffectType.
	 * @param potionEffectType The type of effect for this potion effect.
	 * @return A potion effect with {@link #potionEffectType()} as <code>potionEffectType</code>.
	 * Other properties hold their default values.
	 * @see #fromBukkitEffect(PotionEffect)
	 */
	public static SkriptPotionEffect fromType(PotionEffectType potionEffectType) {
		return new SkriptPotionEffect()
				.potionEffectType(potionEffectType);
	}

	/**
	 * Constructs a SkriptPotionEffect from a Bukkit PotionEffect.
	 * @param potionEffect The potion effect to obtain properties from.
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
	 * Constructs a SkriptPotionEffect from a Bukkit PotionEffect and source entity.
	 * <code>source</code> is expected to currently be affected by <code>potionEffect</code>.
	 * When changes are made to this potion effect, they will be reflected on <code>source</code>.
	 * @param potionEffect The potion effect to obtain properties from.
	 * @param source An entity that should mirror the changes to this potion effect.
	 * @return A potion effect whose properties are set from <code>potionEffect</code>.
	 * @see #fromBukkitEffect(PotionEffect)
	 */
	public static SkriptPotionEffect fromBukkitEffect(PotionEffect potionEffect, LivingEntity source) {
		SkriptPotionEffect skriptPotionEffect = fromBukkitEffect(potionEffect);
		skriptPotionEffect.entitySource = source;
		return skriptPotionEffect;
	}

	/**
	 * Constructs a SkriptPotionEffect from a Bukkit PotionEffect and source item.
	 * <code>source</code> is expected to be an item (potion, stew, etc.) whose meta contains <code>potionEffect</code>.
	 * When changes are made to this potion effect, they will be reflected on <code>source</code>.
	 * @param potionEffect The potion effect to obtain properties from.
	 * @param source An item that should mirror the changes to this potion effect.
	 * @return A potion effect whose properties are set from <code>potionEffect</code>.
	 * @see #fromBukkitEffect(PotionEffect)
	 */
	public static SkriptPotionEffect fromBukkitEffect(PotionEffect potionEffect, ItemType source) {
		SkriptPotionEffect skriptPotionEffect = fromBukkitEffect(potionEffect);
		skriptPotionEffect.itemSource = source;
		return skriptPotionEffect;
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
		withSource(() -> this.potionEffectType = potionEffectType);
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
		if (duration == null) {
			return PotionUtils.DEFAULT_DURATION_TICKS;
		}
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
		withSource(() -> this.duration = duration);
		return this;
	}

	/**
	 * @return The amplifier of this potion effect.
	 * @see PotionEffect#getAmplifier()
	 */
	public int amplifier() {
		if (amplifier == null) {
			return 0;
		}
		return amplifier;
	}

	/**
	 * Updates the amplifier of this potion effect.
	 * @param amplifier The new amplifier of this potion effect.
	 * @return This potion effect.
	 */
	@Contract("_ -> this")
	public SkriptPotionEffect amplifier(int amplifier) {
		lastEffect = null;
		withSource(() -> this.amplifier = amplifier);
		return this;
	}

	/**
	 * @return Whether this potion effect is ambient.
	 * @see PotionEffect#isAmbient()
	 */
	public boolean ambient() {
		if (ambient == null) {
			return false;
		}
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
		withSource(() -> this.ambient = ambient);
		return this;
	}

	/**
	 * @return Whether this potion effect has particles.
	 * @see PotionEffect#hasParticles()
	 */
	public boolean particles() {
		if (particles == null) {
			return true;
		}
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
		withSource(() -> this.particles = particles);
		return this;
	}

	/**
	 * @return Whether this potion effect has an icon.
	 * @see PotionEffect#hasIcon()
	 */
	public boolean icon() {
		if (icon == null) {
			return true;
		}
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
		withSource(() -> this.icon = icon);
		return this;
	}

	/**
	 * Constructs a Bukkit {@link PotionEffect} from this potion effect.
	 * @return A Bukkit PotionEffect representing the values of this potion effect.
	 * Note that the returned value may be the same across multiple calls,
	 *  assuming that this potion effect's values have not changed.
	 */
	public PotionEffect asBukkitPotionEffect() {
		if (lastEffect == null) {
			lastEffect = new PotionEffect(potionEffectType(), duration(), amplifier(), ambient(), particles(), icon());
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
		StringJoiner joiner = new StringJoiner(" ");
		boolean infinite = infinite();
		if (infinite) {
			joiner.add("infinite");
		}
		if (ambient()) {
			joiner.add("ambient");
		}
		joiner.add("potion effect of")
			.add(Classes.toString(potionEffectType()))
			.add(String.valueOf(amplifier() + 1));
		if (!particles()) {
			joiner.add("without particles");
		}
		if (!icon()) {
			joiner.add("without an icon");
		}
		if (duration != null && !infinite) {
			joiner.add("for")
					.add(new Timespan(TimePeriod.TICK, duration()).toString());
		}
		return joiner.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SkriptPotionEffect otherPotion)) {
			return false;
		}
		return this.potionEffectType().equals(otherPotion.potionEffectType())
				&& this.duration() == otherPotion.duration()
				&& this.amplifier() == otherPotion.amplifier()
				&& this.ambient() == otherPotion.ambient()
				&& this.particles() == otherPotion.particles()
				&& this.icon() == otherPotion.icon();
	}

	/**
	 * Determines whether a potion effect has (at least) all the qualities of this potion effect.
	 * @param potionEffect The potion effect whose qualities will be checked.
	 * @return Whether {@code potionEffect} has all the qualities of this potion effect.
	 * Note that {@code potionEffect} may have additional qualities.
	 */
	public boolean matchesQualities(PotionEffect potionEffect) {
		return potionEffectType() == potionEffect.getType() &&
				(duration == null || duration() == potionEffect.getDuration()) &&
				(amplifier == null || amplifier() == potionEffect.getAmplifier()) &&
				(ambient == null || ambient() == potionEffect.isAmbient()) &&
				(particles == null || particles() == potionEffect.hasParticles()) &&
				(icon == null || icon() == potionEffect.hasIcon());
	}

	/*
	 * Source Utilities
	 */

	private void withSource(Runnable runnable) {
		Deque<PotionEffect> hiddenEffects = null;
		if (entitySource != null && entitySource.hasPotionEffect(potionEffectType)) {
			//noinspection DataFlowIssue - NotNull by hasPotionEffect check
			hiddenEffects = PotionUtils.getHiddenEffects(entitySource.getPotionEffect(potionEffectType));
			entitySource.removePotionEffect(potionEffectType);
		} else if (itemSource != null) {
			PotionUtils.removePotionEffects(itemSource, potionEffectType);
		}
		runnable.run();
		if (entitySource != null) {
			PotionEffect thisPotionEffect = asBukkitPotionEffect();
			if (hiddenEffects != null) { // reapply hidden effects
				for (PotionEffect hiddenEffect : hiddenEffects) {
					// we need to add this potion effect in the right order
					// it might end up not being applied at all, but we'll let the game determine that
					if (thisPotionEffect != null &&
						(hiddenEffect.isShorterThan(thisPotionEffect) || hiddenEffect.getAmplifier() > thisPotionEffect.getAmplifier())) {
						entitySource.addPotionEffect(thisPotionEffect);
						thisPotionEffect = null;
					}
					entitySource.addPotionEffect(hiddenEffect);
				}
			}
			if (thisPotionEffect != null) {
				entitySource.addPotionEffect(asBukkitPotionEffect());
			}
		} else if (itemSource != null) {
			PotionUtils.addPotionEffects(itemSource, asBukkitPotionEffect());
		}
	}

	/*
	 * YggdrasilExtendedSerializable
	 */

	@Override
	public Fields serialize() {
		Fields fields = new Fields();
		fields.putObject("type", this.potionEffectType);
		fields.putObject("duration", this.duration);
		fields.putObject("amplifier", this.amplifier);
		fields.putObject("ambient", this.ambient);
		fields.putObject("particles", this.particles);
		fields.putObject("icon", this.icon);
		return fields;
	}

	@Override
	public void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
		potionEffectType = fields.getObject("type", PotionEffectType.class);
		duration = fields.getObject("duration", Integer.class);
		amplifier = fields.getObject("amplifier", Integer.class);
		ambient = fields.getObject("ambient", Boolean.class);
		particles = fields.getObject("particles", Boolean.class);
		icon = fields.getObject("icon", Boolean.class);
	}

	@Override
	public SkriptPotionEffect clone() {
		try {
			SkriptPotionEffect skriptPotionEffect = (SkriptPotionEffect) super.clone();
			// we do not copy over sources on clones
			// for example, copying a potion effect into a variable should not continue tracking the source
			skriptPotionEffect.entitySource = null;
			skriptPotionEffect.itemSource = null;
			return skriptPotionEffect;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}

	/**
	 * Checks whether the potion effects represented by an expression can be modified.
	 * For example, hidden potion effects cannot be modified.
	 * @param expression The expression to check.
	 * @return Whether the potion effects represented by {@code expression} can be modified.
	 * Logs an error if {@code false}.
	 */
	public static boolean isChangeable(Expression<? extends SkriptPotionEffect> expression) {
		if ((expression instanceof ExprPotionEffects exprPotionEffects && exprPotionEffects.getState().includesHidden()) ||
			expression instanceof ExprPotionEffect exprPotionEffect && exprPotionEffect.getState().includesHidden()) {
			Skript.error("Hidden potion effects cannot be changed");
			return false;
		}
		return true;
	}

}
