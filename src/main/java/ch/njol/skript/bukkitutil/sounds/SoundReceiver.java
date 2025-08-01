package ch.njol.skript.bukkitutil.sounds;

import ch.njol.skript.Skript;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.OptionalLong;

/**
 * Adapter pattern to unify {@link World} and {@link Player} playSound methods.
 * Methods can be called without determining version support, it is handled internally.
 * Non-supported methods will simply delegate to supported methods.
 */
public interface SoundReceiver {

	boolean ADVENTURE_API = Skript.classExists("net.kyori.adventure.sound.Sound$Builder") && Skript.methodExists(SoundCategory.class, "soundSource");
	boolean SPIGOT_SOUND_SEED = Skript.methodExists(Player.class, "playSound", Entity.class, Sound.class, SoundCategory.class, float.class, float.class, long.class);
	boolean ENTITY_EMITTER_SOUND = Skript.methodExists(Player.class, "playSound", Entity.class, Sound.class, SoundCategory.class, float.class, float.class);
	boolean ENTITY_EMITTER_STRING = Skript.methodExists(Player.class, "playSound", Entity.class, String.class, SoundCategory.class, float.class, float.class);

	void playSound(Location location, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed);
	void playSound(Entity entity, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed);

	static SoundReceiver of(Player player) { return new PlayerSoundReceiver(player); }
	static SoundReceiver of(World world) { return new WorldSoundReceiver(world); }

	// Player adapter pattern
	class PlayerSoundReceiver implements SoundReceiver {

		private final Player player;

		protected PlayerSoundReceiver(Player player) {
			this.player = player;
		}

		@Override
		public void playSound(Location location, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
			//noinspection DuplicatedCode
			if (ADVENTURE_API) {
				AdventureSoundUtils.playSound(player, location, sound, category, volume, pitch, seed);
			} else if (!SPIGOT_SOUND_SEED || seed.isEmpty()) {
				player.playSound(location, sound.getKey(), category, volume, pitch);
			} else {
				player.playSound(location, sound.getKey(), category, volume, pitch, seed.getAsLong());
			}
		}

		private void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch) {
			//noinspection DuplicatedCode
			if (ENTITY_EMITTER_STRING) {
				player.playSound(entity, sound, category, volume, pitch);
			} else if (ENTITY_EMITTER_SOUND) {
				Sound enumSound;
				try {
					enumSound = Sound.valueOf(sound.replace('.','_').toUpperCase(Locale.ENGLISH));
				} catch (IllegalArgumentException e) {
					return;
				}
				player.playSound(entity, enumSound, category, volume, pitch);
			} else {
				player.playSound(entity.getLocation(), sound, category, volume, pitch);
			}
		}

		@Override
		public void playSound(Entity entity, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
			//noinspection DuplicatedCode
			if (ADVENTURE_API) {
				AdventureSoundUtils.playSound(player, entity, sound, category, volume, pitch, seed);
			} else if (!SPIGOT_SOUND_SEED || seed.isEmpty()) {
				this.playSound(entity, sound.getKey(), category, volume, pitch);
			} else {
				player.playSound(entity, sound.getKey(), category, volume, pitch, seed.getAsLong());
			}
		}
	}

	// World adapter pattern
	class WorldSoundReceiver implements SoundReceiver {

		private final World world;

		protected WorldSoundReceiver(World world) {
			this.world = world;
		}

		@Override
		public void playSound(Location location, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
			//noinspection DuplicatedCode
			if (ADVENTURE_API) {
				AdventureSoundUtils.playSound(world, location, sound, category, volume, pitch, seed);
			} else if (!SPIGOT_SOUND_SEED || seed.isEmpty()) {
				world.playSound(location, sound.getKey(), category, volume, pitch);
			} else {
				world.playSound(location, sound.getKey(), category, volume, pitch, seed.getAsLong());
			}
		}

		private void playSound(Entity entity, String sound, SoundCategory category, float volume, float pitch) {
			//noinspection DuplicatedCode
			if (ENTITY_EMITTER_STRING) {
				world.playSound(entity, sound, category, volume, pitch);
			} else if (ENTITY_EMITTER_SOUND) {
				Sound enumSound;
				try {
					enumSound = Sound.valueOf(sound.replace('.','_').toUpperCase(Locale.ENGLISH));
				} catch (IllegalArgumentException e) {
					return;
				}
				world.playSound(entity, enumSound, category, volume, pitch);
			} else {
				world.playSound(entity.getLocation(), sound, category, volume, pitch);
			}
		}

		@Override
		public void playSound(Entity entity, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
			//noinspection DuplicatedCode
			if (ADVENTURE_API) {
				AdventureSoundUtils.playSound(world, entity, sound, category, volume, pitch, seed);
			} else if (!SPIGOT_SOUND_SEED || seed.isEmpty()) {
				this.playSound(entity, sound.getKey(), category, volume, pitch);
			} else {
				world.playSound(entity, sound.getKey(), category, volume, pitch, seed.getAsLong());
			}
		}
	}
}
