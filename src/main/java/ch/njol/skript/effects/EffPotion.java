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
import org.jetbrains.annotations.Nullable;

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

@Name("Potion Effects")
@Description("Apply or remove potion effects to/from entities.")
@Examples({
	"apply ambient swiftness 2 to the player",
	"remove haste from the victim",
	"",
	"on join:",
		"\tapply infinite potion of strength of tier {strength::%uuid of player%} to the player",
		"\tapply potion of strength of tier {strength::%uuid of player%} to the player for 999 days # Before 1.19.4",
	"",
	"apply potion effects of player's tool to player",
	"apply haste potion of tier 3 without any particles whilst hiding the potion icon to the player # Hide potions"
})
@Since(
	"2.0, 2.2-dev27 (ambient and particle-less potion effects), " + 
	"2.5 (replacing existing effect), 2.5.2 (potion effects), " +
	"2.7 (icon and infinite)"
)
public class EffPotion extends Effect {

	static {
		Skript.registerEffect(EffPotion.class,
				"apply %potioneffects% to %livingentities%",
				"apply infinite [:ambient] [potion of] %potioneffecttypes% [potion] [[[of] tier] %-number%] [noparticles:without [any] particles] [icon:(whilst hiding [the]|without (the|a)) [potion] icon] to %livingentities% [replacing:replacing [the] existing effect]",
				"apply [:ambient] [potion of] %potioneffecttypes% [potion] [[[of] tier] %-number%] [noparticles:without [any] particles] [icon:(whilst hiding [the]|without (the|a)) [potion] icon] to %livingentities% [for %-timespan%] [replacing:replacing [the] existing effect]"
		);
	}

	private final static boolean COMPATIBLE = Skript.isRunningMinecraft(1, 19, 4);

	private final static int DEFAULT_DURATION = 15 * 20; // 15 seconds, same as EffPoison

	private Expression<PotionEffectType> potions;
	private Expression<LivingEntity> entities;
	private Expression<PotionEffect> effects;

	@Nullable
	private Expression<Timespan> duration;

	@Nullable
	private Expression<Number> tier;

	private boolean replaceExisting; // Replace the existing potion if present.
	private boolean potionEffect; // PotionEffects rather than PotionEffectTypes.
	private boolean noParticles;
	private boolean infinite; // 1.19.4+ has an infinite option.
	private boolean ambient; // Ambient means less particles
	private boolean icon;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		potionEffect = matchedPattern == 0;
		replaceExisting = parseResult.hasTag("replacing");
		noParticles = parseResult.hasTag("noparticles");
		ambient = parseResult.hasTag("ambient");
		icon = !parseResult.hasTag("icon");
		infinite = matchedPattern == 1;
		if (potionEffect) {
			effects = (Expression<PotionEffect>) exprs[0];
			entities = (Expression<LivingEntity>) exprs[1];
		} else {
			potions = (Expression<PotionEffectType>) exprs[0];
			tier = (Expression<Number>) exprs[1];
			entities = (Expression<LivingEntity>) exprs[2];
			if (!infinite)
				duration = (Expression<Timespan>) exprs[3];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (potionEffect) {
			for (LivingEntity livingEntity : entities.getArray(event))
				PotionEffectUtils.addEffects(livingEntity, effects.getArray(event));
		} else {
			PotionEffectType[] potionEffectTypes = potions.getArray(event);
			if (potionEffectTypes.length == 0)
				return;
			int tier = 0;
			if (this.tier != null)
				tier = this.tier.getOptionalSingle(event).orElse(1).intValue() - 1;

			int duration = infinite ? (COMPATIBLE ? -1 : Integer.MAX_VALUE) : DEFAULT_DURATION;
			if (this.duration != null && !infinite) {
				Timespan timespan = this.duration.getSingle(event);
				if (timespan == null)
					return;
				duration = (int) Math.min(timespan.getTicks(), Integer.MAX_VALUE);
			}
			for (LivingEntity entity : entities.getArray(event)) {
				for (PotionEffectType potionEffectType : potionEffectTypes) {
					int finalDuration = duration;
					if (!replaceExisting && !infinite) {
						if (entity.hasPotionEffect(potionEffectType)) {
							for (PotionEffect effect : entity.getActivePotionEffects()) {
								if (effect.getType() == potionEffectType) {
									finalDuration += effect.getDuration();
									break;
								}
							}
						}
					}
					entity.addPotionEffect(new PotionEffect(potionEffectType, finalDuration, tier, ambient, !noParticles, icon));
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (potionEffect) {
			// Uses PotionEffectUtils#toString
			return "apply " + effects.toString(event, debug) + " to " + entities.toString(event, debug);
		} else {
			return "apply " + (infinite ? " infinite " : "") +
					potions.toString(event, debug) +
					(tier != null ? " of tier " + tier.toString(event, debug) : "") +
					" to " + entities.toString(event, debug) +
					(duration != null ? " for " + duration.toString(event, debug) : "");
		}
	}

}
