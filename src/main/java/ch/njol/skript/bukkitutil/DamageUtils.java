package ch.njol.skript.bukkitutil;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

public class DamageUtils {

	@SuppressWarnings("UnstableApiUsage")
	public static @NotNull DamageSource getDamageSourceFromCause(DamageCause cause) {
		return DamageSource.builder(switch (cause) {
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
		}).build();
	}

}
