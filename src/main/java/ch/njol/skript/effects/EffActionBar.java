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
import ch.njol.skript.lang.SkriptParser;
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
		Skript.registerEffect(EffActionBar.class, "send [the] action[ ]bar [with text] %string% to %players%");
	}

	@SuppressWarnings("null")
	private Expression<String> message;

	@SuppressWarnings("null")
	private Expression<Player> recipients;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
		message = (Expression<String>) exprs[0];
		recipients = (Expression<Player>) exprs[1];
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void execute(final Event e) {
		String msg = message.getSingle(e);
		assert msg != null;
		BaseComponent[] components = BungeeConverter.convert(ChatMessages.parseToArray(msg));
		for (Player player : recipients.getArray(e))
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "send action bar " + message.toString(e, debug) + " to " + recipients.toString(e, debug);
	}

}
