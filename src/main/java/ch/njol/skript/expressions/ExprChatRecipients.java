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
package ch.njol.skript.expressions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Chat Recipients")
@Description("Recipients of chat/broadcast events where this is called.")
@Examples("chat recipients")
@Since("2.2-Fixes-v7, 2.2-dev35 (clearing recipients), INSERT VERSION (broadcast event)")
public class ExprChatRecipients extends SimpleExpression<CommandSender> {

	static {
		Skript.registerExpression(ExprChatRecipients.class, CommandSender.class, ExpressionType.SIMPLE, "[chat][( |-)]recipients");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!(getParser().isCurrentEvent(AsyncPlayerChatEvent.class, BroadcastMessageEvent.class))) {
			Skript.error("Cannot use chat recipients expression outside of a chat or server broadcast event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected CommandSender[] get(Event event) {
		if (event instanceof AsyncPlayerChatEvent) {
			return ((AsyncPlayerChatEvent) event).getRecipients().toArray(new Player[0]);
		} else if (event instanceof BroadcastMessageEvent) {
			return ((BroadcastMessageEvent) event).getRecipients().toArray(new CommandSender[0]);
		}
		return null;
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(CommandSender[].class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof AsyncPlayerChatEvent) && !(event instanceof BroadcastMessageEvent))
			return;

		CommandSender[] recipients = (CommandSender[]) delta;
		switch (mode) {
			case REMOVE:
				assert recipients != null;
				if (event instanceof AsyncPlayerChatEvent) {
					for (CommandSender sender : recipients)
						((AsyncPlayerChatEvent) event).getRecipients().remove(sender);
				} else {
					for (CommandSender sender : recipients)
						((BroadcastMessageEvent) event).getRecipients().remove(sender);
				}
				break;
			case ADD:
				assert recipients != null;
				if (event instanceof AsyncPlayerChatEvent) {
					for (CommandSender sender : recipients) {
						if (sender instanceof Player)
							((AsyncPlayerChatEvent) event).getRecipients().add((Player) sender);
					}
				} else {
					for (CommandSender sender : recipients)
						((BroadcastMessageEvent) event).getRecipients().add(sender);
				}
				break;
			case SET:
				change(event, delta, ChangeMode.DELETE);
				change(event, delta, ChangeMode.ADD);
				break;
			case REMOVE_ALL:
			case RESET:
			case DELETE:
				if (event instanceof AsyncPlayerChatEvent) {
					((AsyncPlayerChatEvent) event).getRecipients().clear();
				} else {
					((BroadcastMessageEvent) event).getRecipients().clear();
				}
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<CommandSender> getReturnType() {
		return CommandSender.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "chat recipients";
	}

}
