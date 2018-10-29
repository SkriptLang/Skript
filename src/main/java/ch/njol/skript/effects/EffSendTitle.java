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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.effects;

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
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

@Name("Send Title/Subtitle")
@Description({"Sends a title/subtitle to the given player(s) with optional fadein/stay/fadeout times for Minecraft versions 1.11 and above.",
		"Note: if no input is given for the title/subtitle or the times," +
		"it will keep the ones from the last title sent, use the <a href='effects.html#EffResetTitle'>reset title</a> effect to restore the default values."})
@Examples({"send title \"Competition Started\" with subtitle \"Have fun, Stay safe!\" to player for 5 seconds",
		"send title \"Hi %player%\" to player", "send title \"Loot Drop\" with subtitle \"starts in 3 minutes\" to all players",
		"send title \"Hello %player%!\" with subtitle \"Welcome to our server\" to player for 5 seconds with fadein 1 second and fade out 1 second",
		"send subtitle \"Party!\" to all players"})
@Since("INSERT VERSION")
public class EffSendTitle extends Effect {
	
	private final static boolean TIME_SUPPORTED = Skript.methodExists(Player.class,"sendTitle", String.class, String.class, int.class, int.class, int.class);
	
	static {
		if (TIME_SUPPORTED)
			Skript.registerEffect(EffSendTitle.class,
					"send title %string% [with subtitle %-string%] [to %players%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [(and|with) fade[(-| )]out %-timespan%]",
					"send subtitle %string% [to %players%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [(and|with) fade[(-| )]out %-timespan%]");
		else
			Skript.registerEffect(EffSendTitle.class, "send title %string% [with subtitle %-string%] [to %players%]",
												"send subtitle %string% [to %players%]");
		
	}
	
	@Nullable
	private Expression<String> title;
	@Nullable
	private Expression<String> subtitle;
	@SuppressWarnings("null")
	private Expression<Player> recipients;
	@Nullable
	private Expression<Timespan> fadeIn, stay, fadeOut;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		title = matchedPattern == 0 ? (Expression<String>) exprs[0] : null;
		subtitle = matchedPattern == 0 ? (Expression<String>) exprs[1] : (Expression<String>) exprs[0];
		recipients = matchedPattern == 0 ? (Expression<Player>) exprs[2] : (Expression<Player>) exprs[1];
		if (TIME_SUPPORTED) {
			stay = matchedPattern == 0 ? (Expression<Timespan>) exprs[3] : (Expression<Timespan>) exprs[2];
			fadeIn = matchedPattern == 0 ? (Expression<Timespan>) exprs[4] : (Expression<Timespan>) exprs[3];
			fadeOut = matchedPattern == 0 ? (Expression<Timespan>) exprs[5] : (Expression<Timespan>) exprs[4];
		}
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected void execute(final Event e) {
		String title = this.title != null ? this.title.getSingle(e) : "",
		sub = subtitle != null ? subtitle.getSingle(e) : null;
		
		if (TIME_SUPPORTED) {
			int in = fadeIn != null ? (int) fadeIn.getSingle(e).getTicks_i() : -1,
			stay = this.stay != null ? (int) this.stay.getSingle(e).getTicks_i() : -1,
			out = fadeOut != null ? (int) fadeOut.getSingle(e).getTicks_i() : -1;
			
			for (Player p : recipients.getArray(e))
				p.sendTitle(title, sub, in, stay, out);
		} else {
			for (Player p : recipients.getArray(e))
				p.sendTitle(title, sub);
		}
	}
	
	// TODO: util method to simplify this
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		String title = this.title != null ? this.title.toString(e, debug) : "",
		sub = subtitle != null ? subtitle.toString(e, debug) : "",
		in = fadeIn != null ? fadeIn.toString(e, debug) : "",
		stay = this.stay != null ? this.stay.toString(e, debug) : "",
		out = fadeOut != null ? this.fadeOut.toString(e, debug) : "";
		return (title == null ? "send subtitle " + sub : "send title " + title +
				sub == "" ? "" : " with subtitle " + sub) + " to " +
				recipients.toString(e, debug) + (TIME_SUPPORTED ?
				" for " + stay + " with fade in " + in + " and fade out" + out : "");
	}
	
}