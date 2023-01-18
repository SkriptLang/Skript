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
package ch.njol.skript.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Potion Effects")
@Description("Apply or remove potion effects to/from entities.")
@Examples({"apply swiftness 2 to the player",
	"remove haste from the victim",
	"on join:",
	"\tapply potion of strength of tier {strength.%player%} to the player for 999 days",
	"apply potion effects of player's tool to player"})
@Since("2.0, 2.2-dev27 (ambient and particle-less potion effects), 2.5 (replacing existing effect), 2.5.2 (potion effects)")
public class EffPotion extends Effect {
	static {
		Skript.registerEffect(EffPotion.class,
			"apply %potioneffects% to %livingentities%",
			"apply [potion of] %potioneffecttypes% [potion] [[[of] tier] %-number%] to %livingentities% [for %-timespan%] [(1¦replacing [the] existing effect)]",
			"apply ambient [potion of] %potioneffecttypes% [potion] [[[of] tier] %-number%] to %livingentities% [for %-timespan%] [(1¦replacing [the] existing effect)]",
			"apply [potion of] %potioneffecttypes% [potion] [[[of] tier] %-number%] without [any] particles to %livingentities% [for %-timespan%] [(1¦replacing [the] existing effect)]"
			//, "apply %itemtypes% to %livingentities%"
			/*,"remove %potioneffecttypes% from %livingentities%"*/);
	}

	private final static int DEFAULT_DURATION = 15 * 20; // 15 seconds, same as EffPoison
	private boolean replaceExisting;

	@SuppressWarnings("null")
	private Expression<PotionEffectType> potions;
	@Nullable
	private Expression<Number> tier;
	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	@Nullable
	private Expression<Timespan> duration;
	@SuppressWarnings("null")
	private Expression<PotionEffect> potionEffects;
	private boolean apply;
	private boolean ambient; // Ambient means less particles
	private boolean particles; // Particles or no particles?
	private boolean potionEffect; // PotionEffects rather than PotionEffectTypes

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		apply = matchedPattern > 0;
		potionEffect = matchedPattern == 0;
		replaceExisting = parseResult.mark == 1;
		if (potionEffect) {
			potionEffects = (Expression<PotionEffect>) exprs[0];
			entities = (Expression<LivingEntity>) exprs[1];
		} else if (apply) {
			potions = (Expression<PotionEffectType>) exprs[0];
			tier = (Expression<Number>) exprs[1];
			entities = (Expression<LivingEntity>) exprs[2];
			duration = (Expression<Timespan>) exprs[3];
		} else {
			potions = (Expression<PotionEffectType>) exprs[0];
			entities = (Expression<LivingEntity>) exprs[1];
		}

		// Ambience and particles
		switch (matchedPattern) {
			case 1:
				ambient = false;
				particles = true;
				break;
			case 2:
				ambient = true;
				particles = true;
				break;
			case 3:
				ambient = false;
				particles = false;
				break;
		}

		return true;
	}

	@Override
	protected void execute(final Event e) {
		if (potionEffect) {
			for (LivingEntity livingEntity : entities.getArray(e)) {
				PotionEffect[] potionEffects = this.potionEffects.getArray(e);
				PotionEffectUtils.addEffects(livingEntity, potionEffects);
			}
		} else {
			final PotionEffectType[] ts = potions.getArray(e);
			if (ts.length == 0)
				return;
			if (!apply) {
				for (LivingEntity en : entities.getArray(e)) {
					for (final PotionEffectType t : ts)
						en.removePotionEffect(t);
				}
				return;
			}
			int a = 0;
			if (tier != null) {
				final Number amp = tier.getSingle(e);
				if (amp == null)
					return;
				a = amp.intValue() - 1;
			}
			int d = DEFAULT_DURATION;
			if (duration != null) {
				final Timespan dur = duration.getSingle(e);
				if (dur == null)
					return;
				d = (int) (dur.getTicks_i() >= Integer.MAX_VALUE ? Integer.MAX_VALUE : dur.getTicks_i());
			}
			for (final LivingEntity en : entities.getArray(e)) {
				for (final PotionEffectType t : ts) {
					int duration = d;
					if (!replaceExisting) {
						if (en.hasPotionEffect(t)) {
							for (final PotionEffect eff : en.getActivePotionEffects()) {
								if (eff.getType() == t) {
									duration += eff.getDuration();
									break;
								}
							}
						}
					}
					en.addPotionEffect(new PotionEffect(t, duration, a, ambient, particles), true);
				}
			}
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (potionEffect)
			return "apply " + potionEffects.toString(e, debug) + " to " + entities.toString(e, debug);
		else if (apply)
			return "apply " + potions.toString(e, debug) + (tier != null ? " of tier " + tier.toString(e, debug) : "") + " to " + entities.toString(e, debug) + (duration != null ? " for " + duration.toString(e, debug) : "");
		else
			return "remove " + potions.toString(e, debug) + " from " + entities.toString(e, debug);
	}

}
