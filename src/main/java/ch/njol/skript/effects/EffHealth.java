package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.DamageUtils;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Damageable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceWrapper;

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
@Since("1.0, 2.10 (damage cause)")
@RequiredPlugins("Spigot 1.20.4+ (for damage cause)")
public class EffHealth extends Effect {

	private enum EffectType {
		DAMAGE, HEAL, REPAIR
	}

	private static final boolean SUPPORTS_DAMAGE_SOURCE = Skript.classExists("org.bukkit.damage.DamageSource");

	private static final Patterns<EffectType> PATTERNS;

	static {
		if (!SUPPORTS_DAMAGE_SOURCE) {
			PATTERNS = new Patterns<>(new Object[][]{
				{"damage %livingentities/itemtypes/slots% by %number% [heart[s]]", EffectType.DAMAGE},
				{"heal %livingentities% [by %-number% [heart[s]]]", EffectType.HEAL},
				{"repair %itemtypes/slots% [by %-number%]", EffectType.REPAIR}
			});
		} else {
			PATTERNS = new Patterns<>(new Object[][]{
				{"damage %livingentities/itemtypes/slots% by %number% [heart[s]] [with [fake] [damage] cause %-damagecause%]", EffectType.DAMAGE},
				{"damage %livingentities/itemtypes/slots% by %number% [heart[s]] (using|with) %damagesource%", EffectType.DAMAGE},
				{"heal %livingentities% [by %-number% [heart[s]]]", EffectType.HEAL},
				{"repair %itemtypes/slots% [by %-number%]", EffectType.REPAIR}
			});
		}

		Skript.registerEffect(EffHealth.class, PATTERNS.getPatterns());
	}

	private Expression<?> damageables;
	private @Nullable Expression<Number> amount = null;
	private EffectType effectType;
	private @Nullable Expression<?> damageCause = null;
	private @Nullable Expression<?> damageSource = null;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		effectType = PATTERNS.getInfo(matchedPattern);
		damageables = exprs[0];
		//noinspection unchecked
		amount = (Expression<Number>) exprs[1];

		if (effectType == EffectType.DAMAGE && SUPPORTS_DAMAGE_SOURCE) {
			if (matchedPattern == 0)  {
				damageCause = exprs[2];
			} else {
				damageSource = exprs[2];
			}
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Double amount = null;
		if (this.amount != null) {
			Number amountPostCheck = this.amount.getSingle(event);
			if (amountPostCheck == null)
				return;
			amount = amountPostCheck.doubleValue();
		}

		Object object = null;
		if (damageCause != null) {
			object = damageCause.getSingle(event);
		} else if (damageSource != null) {
			object = damageSource.getSingle(event);
		}

		for (Object obj : this.damageables.getArray(event)) {
			if (obj instanceof ItemType itemType) {
				handleItemType(itemType, amount);
			} else if (obj instanceof Slot slot) {
				handleSlot(slot, amount);
			} else if (obj instanceof Damageable damageable) {
				handleDamageable(damageable, amount, object);
			}
		}
	}

	private void handleItemType(ItemType itemType, @Nullable Double amount) {
		Integer value = null;
		if (effectType == EffectType.DAMAGE) {
			assert amount != null;
			value = Math2.fit(0, ItemUtils.getDamage(itemType) + amount.intValue(), ItemUtils.getMaxDamage(itemType));
		} else if (effectType == EffectType.REPAIR) {
			if (amount == null) {
				value = 0;
			} else {
				value = Math2.fit(0, ItemUtils.getDamage(itemType) - amount.intValue(), ItemUtils.getMaxDamage(itemType));
			}
		}
		if (value != null)
			ItemUtils.setDamage(itemType, value);
	}

	private void handleSlot(Slot slot, @Nullable Double amount) {
		ItemStack itemStack = slot.getItem();
		if (itemStack == null)
			return;
		Integer value = null;
		if (effectType == EffectType.DAMAGE) {
			assert amount != null;
			value = Math2.fit(0, ItemUtils.getDamage(itemStack) + amount.intValue(), ItemUtils.getMaxDamage(itemStack));
		} else if (effectType == EffectType.REPAIR) {
			if (amount == null) {
				value = 0;
			} else {
				value = Math2.fit(0, ItemUtils.getDamage(itemStack) - amount.intValue(), ItemUtils.getMaxDamage(itemStack));
			}
		}
		if (value != null) {
			ItemUtils.setDamage(itemStack, value);
			slot.setItem(itemStack);
		}
	}

	private void handleDamageable(Damageable damageable, @Nullable Double amount, @Nullable Object object) {
		if (effectType == EffectType.DAMAGE) {
			assert amount != null;
			if (SUPPORTS_DAMAGE_SOURCE && object != null) {
				if (object instanceof DamageCause damageCause) {
					HealthUtils.damage(damageable, amount, DamageUtils.getDamageSourceFromCause(damageCause));
					return;
				} else if (object instanceof DamageSource damageSource) {
					if (damageSource instanceof DamageSourceWrapper wrapper) {
						HealthUtils.damage(damageable, amount, wrapper.build());
					} else {
						HealthUtils.damage(damageable, amount, damageSource);
					}
					return;
				}
			}
			HealthUtils.damage(damageable, amount);
		} else if (effectType == EffectType.HEAL) {
			if (amount == null) {
				HealthUtils.heal(damageable, HealthUtils.getMaxHealth(damageable));
			} else {
				HealthUtils.heal(damageable, amount);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		switch (effectType) {
			case DAMAGE -> {
				assert amount != null;
				builder.append("damage", damageables, "by", amount);
				if (damageCause != null) {
					builder.append("with fake damage cause", damageCause);
				} else if (damageSource != null) {
					builder.append("using", damageSource);
				}
			}
			case HEAL -> {
				builder.append("heal", damageables);
				if (amount != null)
					builder.append("by", amount);
			}
			case REPAIR -> {
				builder.append("repair", damageables);
				if (amount != null)
					builder.append("by", amount);
			}
		}
		return builder.toString();
	}

}
