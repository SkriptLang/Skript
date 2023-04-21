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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Pattern;

@Name("Play Sound")
@Description({"Plays a sound at given location for everyone or just for given players, or plays a sound to specified players. " +
		"Both Minecraft sound names and " +
		"<a href=\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\">Spigot sound names</a> " +
		"are supported. Playing resource pack sounds are supported too. The sound category is 'master' by default. ",
		"",
		"Please note that sound names can get changed in any Minecraft or Spigot version, or even removed from Minecraft itself."})
@Examples({
	"play sound \"block.note_block.pling\" # It is block.note.pling in 1.12.2",
	"play sound \"entity.experience_orb.pickup\" with volume 0.5 to the player",
	"play sound \"custom.music.1\" in jukebox category at {speakerBlock}"
})
@Since("2.2-dev28, 2.4 (sound categories)")
public class EffPlaySound extends Effect {

	private static final Pattern KEY_PATTERN = Pattern.compile("([a-z0-9._-]+:)?[a-z0-9/._-]+");
	
	static {
		Skript.registerEffect(EffPlaySound.class,
				"play sound[s] %strings% [(in|from) %-soundcategory%] " +
						"[(at|with) volume %-number%] [(and|at|with) pitch %-number%] at %locations% [(to|for) %-players%]",
				"play sound[s] %strings% [(in|from) %-soundcategory%] " +
						"[(at|with) volume %-number%] [(and|at|with) pitch %-number%] [(to|for) %players%] [(at|from) %-locations%]"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> sounds;
	@Nullable
	private Expression<SoundCategory> category;
	@Nullable
	private Expression<Number> volume;
	@Nullable
	private Expression<Number> pitch;
	@Nullable
	private Expression<Location> locations;
	@Nullable
	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		sounds = (Expression<String>) exprs[0];
		category = (Expression<SoundCategory>) exprs[1];
		volume = (Expression<Number>) exprs[2];
		pitch = (Expression<Number>) exprs[3];
		if (matchedPattern == 0) {
			locations = (Expression<Location>) exprs[4];
			players = (Expression<Player>) exprs[5];
		} else {
			players = (Expression<Player>) exprs[4];
			locations = (Expression<Location>) exprs[5];
		}
		
		return true;
	}

	@Override
	protected void execute(Event event) {
		SoundCategory category = this.category == null ? SoundCategory.MASTER : this.category.getOptionalSingle(event)
			.orElse(SoundCategory.MASTER);
		float volume = this.volume == null ? 1 : this.volume.getOptionalSingle(event)
			.orElse(1)
			.floatValue();
		float pitch = this.pitch == null ? 1 : this.pitch.getOptionalSingle(event)
			.orElse(1)
			.floatValue();
		
		if (players != null) {
			if (locations == null) {
				for (Player player : players.getArray(event)) {
					SoundReceiver.play(Player::playSound, Player::playSound, player,
						player.getLocation(), sounds.getArray(event), category, volume, pitch);
				}
			} else {
				for (Player player : players.getArray(event)) {
					for (Location location : locations.getArray(event)) {
						SoundReceiver.play(Player::playSound, Player::playSound, player,
							location, sounds.getArray(event), category, volume, pitch);
					}
				}
			}
		} else if (locations != null) {
			for (Location location : locations.getArray(event)) {
				SoundReceiver.play(World::playSound, World::playSound, location.getWorld(),
					location, sounds.getArray(event), category, volume, pitch);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder builder = new StringBuilder()
			.append("play sound ")
			.append(sounds.toString(event, debug));
		
		if (category != null)
			builder.append(" in ").append(category.toString(event, debug));
		
		if (volume != null)
			builder.append(" with volume ").append(volume.toString(event, debug));
		
		if (pitch != null)
			builder.append(" with pitch ").append(pitch.toString(event, debug));
		
		if (locations != null)
			builder.append(" at ").append(locations.toString(event, debug));
		
		if (players != null)
			builder.append(" to ").append(players.toString(event, debug));
		
		return builder.toString();
	}
	
	@FunctionalInterface
	private interface SoundReceiver<T, S> {
		void play(
			@NotNull T receiver, @NotNull Location location, @NotNull S sound,
			@NotNull SoundCategory category, float volume, float pitch
		);
		
		static <T> void play(
			@NotNull SoundReceiver<T, String> stringReceiver,
			@NotNull SoundReceiver<T, Sound> soundReceiver,
			@NotNull T receiver, @NotNull Location location, @NotNull String[] sounds,
			@NotNull SoundCategory category, float volume, float pitch
		) {	
			for (String sound : sounds) {
				try {
					Sound enumSound = Sound.valueOf(sound.toUpperCase(Locale.ENGLISH));
					soundReceiver.play(receiver, location, enumSound, category, volume, pitch);
					continue;
				} catch (IllegalArgumentException ignored) {}
				
				sound = sound.toLowerCase(Locale.ENGLISH);
				if (!KEY_PATTERN.matcher(sound).matches())
					continue;
				
				stringReceiver.play(receiver, location, sound, category, volume, pitch);
			}
		}
	}
}
