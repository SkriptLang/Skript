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

import java.util.Arrays;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;

@Name("Send Sign Change")
@Description({
	"Makes a player see a sign with text on it.",
	"Color/Glowing is 1.17+ and usage of Skript chat options requires Paper spigot."
})
@Examples({
	"set {_progress} to 100 - (100 - {progress::%uuid of player%})",
	"if {_progress} is equal to 100:",
		"\tset {_colour} to lime green",
	"else if {_progress} is greater than 50:",
		"\tset {_colour} to yellow",
	"else if {_progress} is greater than 25:",
		"\tset {_colour} to orange",
	"else",
		"\tset {_colour} to red",
	"make player see sign at {signs::progress} with text \"Your progress is at %{_progress}%\" coloured {_colour}"
})
@Since("INSERT VERSION")
public class EffSendSignChange extends Effect {

	private static final boolean COLORS_SUPPORTED = Skript.methodExists(Player.class, "sendSignChange", Location.class, String[].class, DyeColor.class);

	static {
		String syntax = "make %players% see [sign[s]] %locations% with [text|lines] %strings%";
		if (COLORS_SUPPORTED) {
			syntax += " [(colo[u]red|with color) [outline] %color%] [[with|to be] :glowing]";
		}
		Skript.registerEffect(EffSendSignChange.class, syntax);
	}

	private Expression<Location> locations;
	private Expression<Player> players;
	private Expression<String> lines;

	private Expression<SkriptColor> color;
	private boolean glowing;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		locations = (Expression<Location>) exprs[1];
		lines = (Expression<String>) exprs[2];
		if (COLORS_SUPPORTED) {
			color = (Expression<SkriptColor>) exprs[3];
			glowing = parseResult.hasTag("glowing");
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		String[] lines = this.lines.getArray(event);
		if (lines.length > 4)
			lines = Arrays.copyOfRange(lines, 0, 3);
		Location[] locations = this.locations.getArray(event);
		SkriptColor color = this.color.getSingle(event);
		for (Player player : players.getArray(event)) {
			for (Location location : locations) {
				if (color == null) {
					player.sendSignChange(location, lines);
					continue;
				}
				player.sendSignChange(location, lines, color.asDyeColor(), glowing);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return String.format("make %s see signs at %s with lines %s",
				players.toString(event, debug),
				locations.toString(event, debug),
				lines.toString(event, debug)
		);
	}

}
