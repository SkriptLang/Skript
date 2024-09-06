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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

@Name("Damage/Heal/Repair")
@Description("Damage/Heal/Repair an entity, or item.")
@Examples({
	"damage player by 5 hearts",
	"heal the player",
	"repair tool of player"
})
@Since("1.0")
public class EffHealth extends Effect {

	static {
		Skript.registerEffect(EffHealth.class,
			"damage %livingentities/itemtypes/slots% by %number% [heart[s]] [with fake cause %-damagecause%]",
			"heal %livingentities% [by %-number% [heart[s]]]",
			"repair %itemtypes/slots% [by %-number%]");
	}

	private Expression<?> damageables;
	@Nullable
	private Expression<Number> amount;
	private boolean isHealing, isRepairing;
	private final boolean canSetDamageCause = Skript.classExists("org.bukkit.damage.DamageSource");
	@Nullable
	private Expression<EntityDamageEvent.DamageCause> exprCause;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 && exprs[2] != null && !canSetDamageCause)
			Skript.warning("The fake damage cause extension of this effect has no functionality in your server, it need MC 1.20.4+");

		this.damageables = exprs[0];
		this.isHealing = matchedPattern >= 1;
		this.isRepairing = matchedPattern == 2;
		this.amount = (Expression<Number>) exprs[1];
		this.exprCause = (Expression<EntityDamageEvent.DamageCause>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		double amount = 0;
		if (this.amount != null) {
			Number amountPostCheck = this.amount.getSingle(event);
			if (amountPostCheck == null)
				return;
			amount = amountPostCheck.doubleValue();
		}

		for (Object obj : this.damageables.getArray(event)) {
			if (obj instanceof ItemType) {
				ItemType itemType = (ItemType) obj;

				if (this.amount == null) {
					ItemUtils.setDamage(itemType, 0);
				} else {
					ItemUtils.setDamage(itemType, (int) Math2.fit(0, (ItemUtils.getDamage(itemType) + (isHealing ? -amount : amount)), ItemUtils.getMaxDamage(itemType)));
				}

			} else if (obj instanceof Slot) {
				Slot slot = (Slot) obj;
				ItemStack itemStack = slot.getItem();

				if (itemStack == null)
					continue;

				if (this.amount == null) {
					ItemUtils.setDamage(itemStack, 0);
				} else {
					int damageAmt = (int) Math2.fit(0, (ItemUtils.getDamage(itemStack) + (isHealing ? -amount : amount)), ItemUtils.getMaxDamage(itemStack));
					ItemUtils.setDamage(itemStack, damageAmt);
				}

				slot.setItem(itemStack);

			} else if (obj instanceof Damageable) {
				Damageable damageable = (Damageable) obj;

				if (this.amount == null) {
					HealthUtils.heal(damageable, HealthUtils.getMaxHealth(damageable));
				} else if (isHealing) {
					HealthUtils.heal(damageable, amount);
				} else {
					if (canSetDamageCause && exprCause != null) {
						EntityDamageEvent.DamageCause cause = exprCause.getSingle(event);
						if (cause != null) {
							damageable.damage(amount * 2, DamageSource.builder(switch (cause) {
								case KILL, SUICIDE -> DamageType.GENERIC_KILL;
								case WORLD_BORDER, VOID -> DamageType.OUT_OF_WORLD;
								case CONTACT -> DamageType.CACTUS;
								case SUFFOCATION -> DamageType.IN_WALL;
								case FALL -> DamageType.FALL;
								case FIRE -> DamageType.ON_FIRE;
								case FIRE_TICK -> DamageType.IN_FIRE;
								case LAVA -> DamageType.LAVA;
								case DROWNING -> DamageType.DROWN;
								case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> DamageType.EXPLOSION;
								case LIGHTNING -> DamageType.LIGHTNING_BOLT;
								case STARVATION -> DamageType.STARVE;
								case MAGIC, POISON -> DamageType.MAGIC;
								case WITHER -> DamageType.WITHER;
								case FALLING_BLOCK -> DamageType.FALLING_BLOCK;
								case THORNS -> DamageType.THORNS;
								case DRAGON_BREATH -> DamageType.DRAGON_BREATH;
								case FLY_INTO_WALL -> DamageType.FLY_INTO_WALL;
								case HOT_FLOOR -> DamageType.HOT_FLOOR;
								case CAMPFIRE -> DamageType.CAMPFIRE;
								case CRAMMING -> DamageType.CRAMMING;
								case DRYOUT -> DamageType.DRY_OUT;
								case FREEZE -> DamageType.FREEZE;
								case SONIC_BOOM -> DamageType.SONIC_BOOM;
								default -> DamageType.GENERIC;
							}).build());
							return;
						}
					}
					HealthUtils.damage(damageable, amount);
				}

			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {

		String prefix = "damage ";
		if (isRepairing) {
			prefix = "repair ";
		} else if (isHealing) {
			prefix = "heal ";
		}

		return prefix + damageables.toString(event, debug) + (amount != null ? " by " + amount.toString(event, debug) : "");
	}

}
