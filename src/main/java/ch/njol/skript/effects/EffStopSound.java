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
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Locale;

@Name("Stop Sound")
@Description({"Stops a sound from playing to the specified players. Both Minecraft sound names and " +
		"<a href=\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\">Spigot sound names</a> " +
		"are supported. Resource pack sounds are supported too. The sound category is 'master' by default. " +
		"A sound can't be stopped from a different category. ",
		"",
		"Please note that sound names can get changed in any Minecraft or Spigot version, or even removed from Minecraft itself."})
@Examples({"stop sound \"block.chest.open\" for the player",
		"stop playing sounds \"ambient.underwater.loop\" and \"ambient.underwater.loop.additions\" to the player"})
@Since("2.4")
@RequiredPlugins("Minecraft Minecraft 1.11+ (sound categories), Minecraft 1.17+ (all sounds)")
public class EffStopSound extends Effect {

	static {
		if (Skript.methodExists(Player.class, "stopAllSounds")) {
			Skript.registerEffect(EffStopSound.class,
				"stop sound[s] %strings% [(in|from) %-soundcategory%] [(from playing to|for) %players%]",
				"stop playing sound[s] %strings% [(in|from) %-soundcategory%] [(to|for) %players%]",
				"stop all sound[s] [(from playing to|for) %players%]",
				"stop playing all sound[s] [(to|for) %players%]");
		} else {
			Skript.registerEffect(EffStopSound.class,
				"stop sound[s] %strings% [(in|from) %soundcategory%] [(from playing to|for) %players%]",
				"stop playing sound[s] %strings% [(in|from) %soundcategory%] [(to|for) %players%]");
		}
	}

	@Nullable
	private Expression<String> sounds;
	@Nullable
	private Expression<SoundCategory> category;
	@SuppressWarnings("null")
	private Expression<Player> players;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern < 2) {
			sounds = (Expression<String>) exprs[0];
			category = (Expression<SoundCategory>) exprs[1];
			players = (Expression<Player>) exprs[2];
		} else {
			players = (Expression<Player>) exprs[0];
		}

		return true;
	}

	@Override
	protected void execute(Event e) {
		if (sounds == null) {
			for (Player p : players.getArray(e))
				p.stopAllSounds();
		} else {
			SoundCategory category = null;

			if (this.category != null)
				category = this.category.getSingle(e);

			for (String sound : sounds.getArray(e)) {
				Sound soundEnum = null;
				try {
					soundEnum = Sound.valueOf(sound.toUpperCase(Locale.ENGLISH));
				} catch (IllegalArgumentException ignored) {}

				if (soundEnum == null) {
					for (Player p : players.getArray(e))
						p.stopSound(sound, category);
				} else {
					for (Player p : players.getArray(e))
						p.stopSound(soundEnum, category);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (sounds == null) {
			return "stop all sounds from playing to " + players.toString(e, debug);
		}

		return "stop sound " + sounds.toString(e, debug) +
				(category != null ? " in " + category.toString(e, debug) : "") +
				" from playing to " + players.toString(e, debug);
	}

}
