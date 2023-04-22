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
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
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
import java.util.regex.Pattern;

@Name("Stop Sound")
@Description({
	"Stops specific or all sounds from playing to a group of players. Both Minecraft sound names and " +
	"<a href=\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\">Spigot sound names</a> " +
	"are supported. Resource pack sounds are supported too. The sound category is 'master' by default. " +
	"A sound can't be stopped from a different category. ",
	"",
	"Please note that sound names can get changed in any Minecraft or Spigot version, or even removed from Minecraft itself."
})
@Examples({
	"stop sound \"block.chest.open\" for the player",
	"stop playing sounds \"ambient.underwater.loop\" and \"ambient.underwater.loop.additions\" to the player",
	"stop all sound for all players",
	"stop sound in record category"
})
@Since("2.4, 2.7 (stop all sounds)")
@RequiredPlugins("MC 1.17.1 (stop all sounds)")
public class EffStopSound extends Effect {
	
	private static final boolean STOP_ALL_SUPPORTED = Skript.methodExists(Player.class, "stopAllSounds");
	private static final Pattern KEY_PATTERN = Pattern.compile("([a-z0-9._-]+:)?[a-z0-9/._-]+");

	
	static {
		String stopPattern = STOP_ALL_SUPPORTED ? "(all:all sound[s]|sound[s] %strings%)" : "sound[s] %strings%";
		
		Skript.registerEffect(EffStopSound.class,
			"stop " + stopPattern + " [(in|from) %-soundcategory%] [(from playing to|for) %players%]",
			"stop playing sound[s] %strings% [(in|from) %-soundcategory%] [(to|for) %players%]"
		);
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> sounds;
	@Nullable
	private Expression<SoundCategory> category;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Player> players;
	
	private boolean allSounds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		allSounds = parseResult.hasTag("all");
		if (allSounds) {
			category = (Expression<SoundCategory>) exprs[0];
			players = (Expression<Player>) exprs[1];
		} else {
			sounds = (Expression<String>) exprs[0];
			category = (Expression<SoundCategory>) exprs[1];
			players = (Expression<Player>) exprs[2];
		}
		
		return true;
	}

	@Override
	protected void execute(Event event) {
		// All sounds pattern wants explicitly defined master category
		SoundCategory category = this.category == null ? null : this.category.getOptionalSingle(event)
			.orElse(allSounds ? null : SoundCategory.MASTER);
		Player[] targets = players.getArray(event);
		
		if (allSounds) {
			if (category == null) {
				for (Player player : targets) {
					player.stopAllSounds();
				}
			} else {
				for (Player player : targets) {
					player.stopSound(category);
				}
			}
		} else {
			for (String sound : sounds.getArray(event)) {
				try {
					Sound soundEnum = Sound.valueOf(sound.toUpperCase(Locale.ENGLISH));
					for (Player player : targets) {
						player.stopSound(soundEnum, category);
					}
					
					continue;
				} catch (IllegalArgumentException ignored) { }
				
				sound = sound.toLowerCase(Locale.ENGLISH);
				if (!KEY_PATTERN.matcher(sound).matches())
					continue;
				
				for (Player player : targets) {
					player.stopSound(sound, category);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (allSounds ? "stop all sounds " : "stop sound " + sounds.toString(event, debug)) +
				(category != null ? " in " + category.toString(event, debug) : "") +
				" from playing to " + players.toString(event, debug);
	}

}
