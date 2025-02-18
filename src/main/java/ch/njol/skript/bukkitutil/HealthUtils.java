package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.util.Math2;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;

public class HealthUtils {
	private static final @Nullable Constructor<EntityDamageEvent> OLD_DAMAGE_EVENT_CONSTRUCTOR;

	static {
		Constructor<EntityDamageEvent> constructor = null;
		try {
			constructor = EntityDamageEvent.class.getConstructor(Damageable.class, DamageCause.class, double.class);
		} catch (NoSuchMethodException ignored) {}
		OLD_DAMAGE_EVENT_CONSTRUCTOR = constructor;
	}

	private static final Attribute MAX_HEALTH;
	static {
		if (Skript.isRunningMinecraft(1, 21, 3)) { // In 1.21.3, Attribute became an Interface
			MAX_HEALTH = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health"));
		} else {
			MAX_HEALTH = (Attribute) Enum.valueOf((Class) Attribute.class, "GENERIC_MAX_HEALTH");
		}
	}

	/**
	 * Get the health of an entity
	 *
	 * @param damageable Entity to get health from
	 * @return The amount of hearts the entity has left
	 */
	public static double getHealth(Damageable damageable) {
		if (damageable.isDead())
			return 0;
		return damageable.getHealth() / 2;
	}

	/**
	 * Set the health of an entity
	 *
	 * @param damageable      Entity to set health for
	 * @param health The amount of hearts to set
	 */
	public static void setHealth(Damageable damageable, double health) {
		damageable.setHealth(Math2.fit(0, health, getMaxHealth(damageable)) * 2);
	}

	/**
	 * Get the max health an entity has
	 *
	 * @param damageable Entity to get max health from
	 * @return How many hearts the entity can have at most
	 */
	public static double getMaxHealth(Damageable damageable) {
		AttributeInstance attributeInstance = ((Attributable) damageable).getAttribute(MAX_HEALTH);
		assert attributeInstance != null;
		return attributeInstance.getValue() / 2;
	}

	/**
	 * Set the max health an entity can have
	 *
	 * @param damageable      Entity to set max health for
	 * @param health How many hearts the entity can have at most
	 */
	public static void setMaxHealth(Damageable damageable, double health) {
		AttributeInstance attributeInstance = ((Attributable) damageable).getAttribute(MAX_HEALTH);
		assert attributeInstance != null;
		attributeInstance.setBaseValue(health * 2);
	}

	/**
	 * Apply damage to an entity
	 *
	 * @param damageable Entity to apply damage to
	 * @param damage Amount of hearts to damage
	 */
	public static void damage(Damageable damageable, double damage) {
		if (damage < 0) {
			heal(damageable, -damage);
			return;
		}
		damageable.damage(damage * 2);
	}

	@SuppressWarnings("UnstableApiUsage")
	public static void damage(Damageable damageable, double damage, DamageSource cause) {
		if (damage < 0) {
			heal(damageable, -damage);
			return;
		}
		damageable.damage(damage * 2, cause);
	}

	/**
	 * Heal an entity
	 *
	 * @param damageable Entity to heal
	 * @param health Amount of hearts to heal
	 */
	public static void heal(Damageable damageable, double health) {
		if (health < 0) {
			damage(damageable, -health);
			return;
		}
		setHealth(damageable, getHealth(damageable) + health);
	}

	public static double getDamage(EntityDamageEvent event) {
		return event.getDamage() / 2;
	}

	public static double getFinalDamage(EntityDamageEvent event) {
		return event.getFinalDamage() / 2;
	}

	public static void setDamage(EntityDamageEvent event, double damage) {
		event.setDamage(damage * 2);
		// Set last damage manually as Bukkit doesn't appear to do that
		if (event.getEntity() instanceof LivingEntity)
			((LivingEntity) event.getEntity()).setLastDamage(damage * 2);
	}

	@SuppressWarnings("removal")
	public static void setDamageCause(Damageable damageable, DamageCause cause) {
		if (OLD_DAMAGE_EVENT_CONSTRUCTOR != null) {
			try {
				damageable.setLastDamageCause(OLD_DAMAGE_EVENT_CONSTRUCTOR.newInstance(damageable, cause, 0));
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
				Skript.exception("Failed to set last damage cause");
			}
		} else {
			damageable.setLastDamageCause(new EntityDamageEvent(damageable, cause, DamageSource.builder(DamageType.GENERIC).build(), 0));
		}
	}
}
