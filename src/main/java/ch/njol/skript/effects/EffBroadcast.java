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

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Broadcast")
@Description({"Broadcasts a message to the server. Only formatting options supported by console",
		"(i.e. colors) are supported. If you need to use advanced chat formatting, send the",
		"message to all players instead of broadcasting it."})
@Examples({"broadcast \"Welcome %player% to the server!\"",
		"broadcast \"Woah! It's a message!\""})
@Since("1.0, 2.6 (broadcasting objects)")
public class EffBroadcast extends Effect {

	static {
		Skript.registerEffect(EffBroadcast.class, "broadcast %objects% [(to|in) %-worlds%]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> messages;
	@Nullable
	private Expression<World> worlds;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		messages = LiteralUtils.defendExpression(exprs[0]);
		worlds = (Expression<World>) exprs[1];
		return LiteralUtils.canInitSafely(messages);
	}
	
	@Override
	public void execute(Event e) {
		World[] worlds = this.worlds != null ? this.worlds.getArray(e) : null;

		for (Object object : messages.getArray(e)) {
			String string = object instanceof String ? (String) object : Classes.toString(object);

			if (worlds == null) {
				// not Bukkit.broadcastMessage to ignore permissions
				for (Player player : PlayerUtils.getOnlinePlayers()) {
					player.sendMessage(string);
				}
				Bukkit.getConsoleSender().sendMessage(string);
			} else {
				for (World w : worlds) {
					for (Player player : w.getPlayers()) {
						player.sendMessage(string);
					}
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "broadcast " + messages.toString(e, debug) + (worlds == null ? "" : " to " + worlds.toString(e, debug));
	}
	
}
