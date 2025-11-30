package org.skriptlang.skript.bukkit.particles;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ParticleUtils {

	private static final RegistryKey<Particle> PARTICLE_REGISTRY_KEY = RegistryKey.PARTICLE_TYPE;
	private static final RegistryKeySet<@NotNull Particle> DIRECTIONAL_PARTICLES = RegistrySet.keySetFromValues(PARTICLE_REGISTRY_KEY,
			List.of( // sourced from https://docs.papermc.io/paper/dev/particles/#list-of-directional-particles
				//<editor-fold desc="Directional Particles" defaultstate="collapsed">
				Particle.BLOCK,
				Particle.BUBBLE,
				Particle.BUBBLE_COLUMN_UP,
				Particle.BUBBLE_POP,
				Particle.CAMPFIRE_COSY_SMOKE,
				Particle.CAMPFIRE_SIGNAL_SMOKE,
				Particle.CLOUD,
				Particle.CRIT,
				Particle.DAMAGE_INDICATOR,
				Particle.DRAGON_BREATH,
				Particle.DUST,
				Particle.DUST_COLOR_TRANSITION,
				Particle.DUST_PLUME,
				Particle.ELECTRIC_SPARK,
				Particle.ENCHANTED_HIT,
				Particle.END_ROD,
				Particle.FIREWORK,
				Particle.FISHING,
				Particle.FLAME,
				Particle.FLASH,
				Particle.GLOW_SQUID_INK,
				Particle.ITEM,
				Particle.LARGE_SMOKE,
				Particle.POOF,
				Particle.REVERSE_PORTAL,
				Particle.SCRAPE,
				Particle.SCULK_CHARGE,
				Particle.SCULK_CHARGE_POP,
				Particle.SCULK_SOUL,
				Particle.SMALL_FLAME,
				Particle.SMOKE,
				Particle.SNEEZE,
				Particle.SNOWFLAKE,
				Particle.SOUL,
				Particle.SOUL_FIRE_FLAME,
				Particle.SPIT,
				Particle.SQUID_INK,
				Particle.TOTEM_OF_UNDYING,
				Particle.TRIAL_SPAWNER_DETECTION,
				Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
				Particle.WAX_OFF,
				Particle.WAX_ON,
				Particle.WHITE_SMOKE
				//</editor-fold>
			));
	private static final RegistryKeySet<@NotNull Particle> CONVERGING_PARTICLES = RegistrySet.keySetFromValues(PARTICLE_REGISTRY_KEY,
			List.of( // sourced from https://docs.papermc.io/paper/dev/particles/#list-of-converging-particles
				//<editor-fold desc="Converging Particles" defaultstate="collapsed">
				Particle.ENCHANT,
				Particle.NAUTILUS,
				Particle.OMINOUS_SPAWNING,
				Particle.PORTAL,
				Particle.VAULT_CONNECTION
				//</editor-fold>
			));
	private static final RegistryKeySet<@NotNull Particle> RISING_PARTICLES = RegistrySet.keySetFromValues(PARTICLE_REGISTRY_KEY,
			List.of( // sourced from https://docs.papermc.io/paper/dev/particles/#list-of-rising-particles
				//<editor-fold desc="Rising Particles" defaultstate="collapsed">
				Particle.EFFECT,
				Particle.ENTITY_EFFECT,
				Particle.GLOW,
				Particle.INFESTED,
				Particle.INSTANT_EFFECT,
				Particle.RAID_OMEN,
				Particle.TRIAL_OMEN,
				Particle.WITCH
				//</editor-fold>
			));
	private static final RegistryKeySet<@NotNull Particle> SCALABLE_PARTICLES = RegistrySet.keySetFromValues(PARTICLE_REGISTRY_KEY,
			List.of(
				//<editor-fold desc="Scalable Particles" defaultstate="collapsed">
				Particle.SWEEP_ATTACK,
				Particle.EXPLOSION
				//</editor-fold>
			));


	private static Collection<Particle> directionalParticlesCache = null;
	private static Collection<Particle> convergingParticlesCache = null;
	private static Collection<Particle> risingParticlesCache = null;
	private static Collection<Particle> scalableParticlesCache = null;


	public static boolean isDirectional(@NotNull Particle particle) {
		if (directionalParticlesCache == null)
			directionalParticlesCache = DIRECTIONAL_PARTICLES.resolve(Registry.PARTICLE_TYPE);
		return directionalParticlesCache.contains(particle);
	}

	public static boolean isConverging(@NotNull Particle particle) {
		if (convergingParticlesCache == null)
			convergingParticlesCache = CONVERGING_PARTICLES.resolve(Registry.PARTICLE_TYPE);
		return convergingParticlesCache.contains(particle);
	}

	public static boolean isRising(@NotNull Particle particle) {
		if (risingParticlesCache == null)
			risingParticlesCache = RISING_PARTICLES.resolve(Registry.PARTICLE_TYPE);
		return risingParticlesCache.contains(particle);
	}

	public static boolean isScalable(@NotNull Particle particle) {
		if (scalableParticlesCache == null)
			scalableParticlesCache = SCALABLE_PARTICLES.resolve(Registry.PARTICLE_TYPE);
		return scalableParticlesCache.contains(particle);
	}

	public static boolean usesVelocity(@NotNull Particle particle) {
		return isDirectional(particle) || isRising(particle);
	}

	public static @Unmodifiable @NotNull Collection<Particle> getDirectionalParticles() {
		if (directionalParticlesCache == null)
			directionalParticlesCache = DIRECTIONAL_PARTICLES.resolve(Registry.PARTICLE_TYPE);
		return directionalParticlesCache;
	}

	public static @Unmodifiable @NotNull Collection<Particle> getConvergingParticles() {
		if (convergingParticlesCache == null)
			convergingParticlesCache = CONVERGING_PARTICLES.resolve(Registry.PARTICLE_TYPE);
		return convergingParticlesCache;
	}

	public static @Unmodifiable @NotNull Collection<Particle> getRisingParticles() {
		if (risingParticlesCache == null)
			risingParticlesCache = RISING_PARTICLES.resolve(Registry.PARTICLE_TYPE);
		return risingParticlesCache;
	}

	public static @Unmodifiable @NotNull Collection<Particle> getScalableParticles() {
		if (scalableParticlesCache == null)
			scalableParticlesCache = SCALABLE_PARTICLES.resolve(Registry.PARTICLE_TYPE);
		return scalableParticlesCache;
	}

}
