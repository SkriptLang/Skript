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
package ch.njol.skript.bukkitutil;

import java.util.Locale;
import java.util.OptionalLong;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import ch.njol.skript.effects.EffPlaySound;

/**
 * A utility interface to access the Player::playSound while also providing the same arguments to World::playSound
 * Used in EffPlaySound. Separated due to static versioning.
 */
@FunctionalInterface
public interface AdventureSoundReceiver<T, E> {

	void play(
		@NotNull T receiver, @NotNull E emitter, @NotNull String sound,
		@NotNull SoundCategory category, float volume, float pitch
	);

	static <T, E> void play(
		@NotNull AdventureEmitterSoundReceiver<T> adventureLocationReceiver,
		@NotNull AdventureEntitySoundReceiver<T> adventureEmitterReceiver,
		@NotNull T receiver, @NotNull E emitter, @NotNull String[] sounds,
		@NotNull SoundCategory category, float volume, float pitch, OptionalLong seed
	) {
		for (String sound : sounds) {
			NamespacedKey key = null;
			try {
				Sound enumSound = Sound.valueOf(sound.toUpperCase(Locale.ENGLISH));
				key = enumSound.getKey();
			} catch (IllegalArgumentException alternative) {
				sound = sound.toLowerCase(Locale.ENGLISH);
				if (!EffPlaySound.KEY_PATTERN.matcher(sound).matches())
					continue;
				try {
					key = NamespacedKey.fromString(sound);
				} catch (IllegalArgumentException argument) {
					// The user input invalid characters
				}
			}

			if (key == null)
				continue;
			net.kyori.adventure.sound.Sound adventureSound = net.kyori.adventure.sound.Sound.sound()
					.source(category)
					.volume(volume)
					.pitch(pitch)
					.seed(seed)
					.type(key)
					.build();
			AdventureEmitterSoundReceiver.play(adventureLocationReceiver, adventureEmitterReceiver, receiver, adventureSound, emitter);
		}
	}

	@FunctionalInterface
	public interface AdventureEmitterSoundReceiver<T> {
		void play(
			@NotNull T receiver, @NotNull net.kyori.adventure.sound.Sound sound, double x, double y, double z
		);

		static <T, E> void play(
			@NotNull AdventureEmitterSoundReceiver<T> locationReceiver,
			@NotNull AdventureEntitySoundReceiver<T> emitterReceiver,
			@NotNull T receiver, @NotNull net.kyori.adventure.sound.Sound sound, @NotNull E emitter
		) {
			if (emitter instanceof Location) {
				Location location = (Location) emitter;
				locationReceiver.play(receiver, sound, location.getX(), location.getY(), location.getZ());
			} else if (emitter instanceof Entity) {
				Entity entity = (Entity) emitter;
				emitterReceiver.play(receiver, sound, entity);
			}
		}
	}

	@FunctionalInterface
	public interface AdventureEntitySoundReceiver<T> {
		void play(
			@NotNull T receiver, @NotNull net.kyori.adventure.sound.Sound sound, net.kyori.adventure.sound.Sound.Emitter emitter
		);
	}

}
