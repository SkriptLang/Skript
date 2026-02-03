package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.DamageUtils;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
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
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

import java.util.function.Consumer;

@Name("Damage/Heal/Repair")
@Description({
	"Damage, heal, or repair an entity or item.",
	"Servers running Spigot 1.20.4+ can optionally choose to specify a fake damage cause."
})
@Example("damage player by 5 hearts")
@Example("damage player by 3 hearts with fake cause fall")
@Example("heal the player")
@Example("repair tool of player")
@Since("1.0, 2.10 (damage cause)")
@RequiredPlugins("Spigot 1.20.4+ (for damage cause)")
public class EffHealth extends Effect implements SyntaxRuntimeErrorProducer {

	private enum EffectType {
		DAMAGE, HEAL, REPAIR
	}

	// TODO: Remove when 1.20.4+ is minimum supported
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
				{"damage %livingentities/itemtypes/slots% by %number% [heart[s]]", EffectType.DAMAGE},
				{"damage %livingentities% by %number% [heart[s]] with [fake] [damage] cause %damagecause%", EffectType.DAMAGE},
				{"damage %livingentities% by %number% [heart[s]] (using|with) %damagesource% [as the source]", EffectType.DAMAGE},
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
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		effectType = PATTERNS.getInfo(matchedPattern);
		damageables = exprs[0];
		//noinspection unchecked
		amount = (Expression<Number>) exprs[1];

		if (effectType == EffectType.DAMAGE && SUPPORTS_DAMAGE_SOURCE) {
			if (matchedPattern == 1)  {
				damageCause = exprs[2];
			} else if (matchedPattern == 2) {
				damageSource = exprs[2];
			}
		}
		node = getParser().getNode();
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

		Object damageData = null;
		if (damageCause != null) {
			damageData = damageCause.getSingle(event);
		} else if (damageSource != null) {
			damageData = damageSource.getSingle(event);
		}

		for (Object obj : this.damageables.getArray(event)) {
			if (obj instanceof ItemType itemType) {
				handleItem(itemType.getRandom(), amount, integer -> ItemUtils.setDamage(itemType, integer));
			} else if (obj instanceof Slot slot) {
				ItemStack itemStack = slot.getItem();
				if (itemStack == null)
					continue;
				handleItem(itemStack, amount, integer -> {
					ItemUtils.setDamage(itemStack, integer);
					slot.setItem(itemStack);
				});
			} else if (obj instanceof Damageable damageable) {
				handleDamageable(damageable, amount, damageData);
			}
		}
	}

	private void handleItem(ItemStack itemStack, @Nullable Double amount, Consumer<Integer> consumer) {
		Integer value = null;
		if (effectType == EffectType.DAMAGE) {
			assert amount != null;
			value = Math2.fit(0, ItemUtils.getDamage(itemStack) + amount.intValue(), ItemUtils.getMaxDamage(itemStack));
		} else if (effectType == EffectType.REPAIR) {
			value = amount == null ?
				0 : Math2.fit(0, ItemUtils.getDamage(itemStack) - amount.intValue(), ItemUtils.getMaxDamage(itemStack));
		}
		if (value != null)
			consumer.accept(value);
	}

	private void handleDamageable(Damageable damageable, @Nullable Double amount, @Nullable Object object) {
		if (effectType == EffectType.DAMAGE) {
			assert amount != null;
			if (SUPPORTS_DAMAGE_SOURCE && object != null) {
				if (object instanceof DamageCause damageCause) {
					HealthUtils.damage(damageable, amount, DamageUtils.getDamageSourceFromCause(damageCause));
					return;
				} else if (object instanceof DamageSource damageSource) {
					HealthUtils.damage(damageable, amount, damageSource);
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
	public Node getNode() {
		return node;
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
