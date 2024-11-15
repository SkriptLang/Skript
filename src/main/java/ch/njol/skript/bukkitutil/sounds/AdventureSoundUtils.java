package ch.njol.skript.bukkitutil.sounds;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.OptionalLong;

public class AdventureSoundUtils {

	public static Sound getAdventureSound(NamespacedKey key, SoundCategory category, float volume, float pitch, OptionalLong seed) {
		return Sound.sound()
			.source(category)
			.volume(volume)
			.pitch(pitch)
			.seed(seed)
			.type(key)
			.build();
	}

	public static void playSound(World world, Location location, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
		world.playSound(
			AdventureSoundUtils.getAdventureSound(sound, category, volume, pitch, seed),
			location.x(),
			location.y(),
			location.z()
		);
	}

	public static void playSound(World world, Entity entity, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
		world.playSound(
			AdventureSoundUtils.getAdventureSound(sound, category, volume, pitch, seed),
			entity
		);
	}

	public static void playSound(Player player, Location location, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
		player.playSound(
			AdventureSoundUtils.getAdventureSound(sound, category, volume, pitch, seed),
			location.x(),
			location.y(),
			location.z()
		);
	}

	public static void playSound(Player player, Entity entity, NamespacedKey sound, SoundCategory category, float volume, float pitch, OptionalLong seed) {
		player.playSound(
			AdventureSoundUtils.getAdventureSound(sound, category, volume, pitch, seed),
			entity
		);
	}

}
