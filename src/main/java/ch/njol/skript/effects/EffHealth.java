package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.DamageUtils;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.entity.Damageable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Damage/Heal/Repair")
@Description({
	"Damage, heal, or repair an entity or item.",
	"Servers running Spigot 1.20.4+ can optionally choose to specify a fake damage cause."
})
@Examples({
	"damage player by 5 hearts",
	"damage player by 3 hearts with fake cause fall",
	"heal the player",
	"repair tool of player"
})
@Since("1.0, INSERT VERSION (damage cause)")
@RequiredPlugins("Spigot 1.20.4+ (for damage cause)")
public class EffHealth extends Effect {
	private static final boolean SUPPORTS_DAMAGE_SOURCE = Skript.classExists("org.bukkit.damage.DamageSource");

	static {
		Skript.registerEffect(EffHealth.class,
			"damage %livingentities/itemtypes/slots% by %number% [heart[s]] [with [fake] [damage] cause %-damagecause%]",
			"heal %livingentities% [by %-number% [heart[s]]]",
			"repair %itemtypes/slots% [by %-number%]");
	}

	private Expression<?> damageables;
	private @UnknownNullability Expression<Number> amount;
	private boolean isHealing, isRepairing;
	private @UnknownNullability Expression<DamageCause> exprCause = null;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 && exprs[2] != null && !SUPPORTS_DAMAGE_SOURCE) {
			Skript.error("Using the fake cause extension in effect 'damage' requires Spigot 1.20.4+");
			return false;
		}

		this.damageables = exprs[0];
		this.isHealing = matchedPattern >= 1;
		this.isRepairing = matchedPattern == 2;
		this.amount = (Expression<Number>) exprs[1];
		if (exprs.length > 2)
			this.exprCause = (Expression<DamageCause>) exprs[2];
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
			if (obj instanceof ItemType itemType) {

				if (this.amount == null) {
					ItemUtils.setDamage(itemType, 0);
				} else {
					ItemUtils.setDamage(itemType, (int) Math2.fit(0, (ItemUtils.getDamage(itemType) + (isHealing ? -amount : amount)), ItemUtils.getMaxDamage(itemType)));
				}

			} else if (obj instanceof Slot slot) {
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

			} else if (obj instanceof Damageable damageable) {
				if (this.amount == null) {
					HealthUtils.heal(damageable, HealthUtils.getMaxHealth(damageable));
				} else if (isHealing) {
					HealthUtils.heal(damageable, amount);
				} else {
					if (SUPPORTS_DAMAGE_SOURCE) {
						DamageCause cause = exprCause == null ? null : exprCause.getSingle(event);
						if (cause != null) {
							HealthUtils.damage(damageable, amount, DamageUtils.getDamageSourceFromCause(cause));
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
		return prefix + damageables.toString(event, debug)
				   + (amount != null ? " by " + amount.toString(event, debug) : "")
				   + (exprCause != null && event != null ? " with damage cause " + exprCause.getSingle(event) : "");
	}

}
