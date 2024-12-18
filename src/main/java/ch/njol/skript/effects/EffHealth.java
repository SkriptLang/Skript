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
import org.bukkit.entity.Damageable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

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

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 && exprs[2] != null)
			Skript.warning("The fake damage cause extension of this effect has no functionality, " +
				"and will be removed in the future");

		this.damageables = exprs[0];
		this.isHealing = matchedPattern >= 1;
		this.isRepairing = matchedPattern == 2;
		this.amount = (Expression<Number>) exprs[1];
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
