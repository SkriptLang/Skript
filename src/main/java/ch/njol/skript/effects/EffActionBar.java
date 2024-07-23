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
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

@Name("Action Bar")
@Description("Sends an action bar message to the given player(s).")
@Examples("send action bar \"Hello player!\" to player")
@Since("2.3")
public class EffActionBar extends Effect {

	static {
		Skript.registerEffect(EffActionBar.class, "send [the] action[ ]bar [with text] %string% [to %players%]");
	}

	private Expression<String> message;

	private Expression<Player> recipients;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		message = (Expression<String>) exprs[0];
		recipients = (Expression<Player>) exprs[1];
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void execute(Event event) {
		String msg = message.getSingle(event);
		if (msg == null)
			return;
		BaseComponent[] components = BungeeConverter.convert(ChatMessages.parseToArray(msg));
		for (Player player : recipients.getArray(event))
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "send action bar " + message.toString(event, debug) + " to " + recipients.toString(event, debug);
	}

}
