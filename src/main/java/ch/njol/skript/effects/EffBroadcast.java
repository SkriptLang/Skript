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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprColoured;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.Nullable;

@Name("Broadcast")
@Description("Broadcasts a message to the server.")
@Examples({
	"broadcast \"Welcome %player% to the server!\"",
	"broadcast \"Woah! It's a message!\""
})
@Since("1.0, 2.6 (broadcasting objects), 2.6.1 (using advanced formatting)")
public class EffBroadcast extends Effect {

	private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&x((?:&\\p{XDigit}){6})");

	static {
		Skript.registerEffect(EffBroadcast.class, "broadcast %objects% [(to|in) %-worlds%]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> messageExpr;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?>[] messages;
	@Nullable
	private Expression<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		messageExpr = LiteralUtils.defendExpression(exprs[0]);
		messages = messageExpr instanceof ExpressionList ?
			((ExpressionList<?>) messageExpr).getExpressions() : new Expression[] {messageExpr};
		worlds = (Expression<World>) exprs[1];
		return LiteralUtils.canInitSafely(messageExpr);
	}

	/**
	 * This effect will call {@link BroadcastMessageEvent} as of 2.9.0.
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void execute(Event event) {
		List<CommandSender> receivers = new ArrayList<>();
		if (worlds == null) {
			receivers.addAll(Bukkit.getOnlinePlayers());
			receivers.add(Bukkit.getConsoleSender());
		} else {
			for (World world : worlds.getArray(event))
				receivers.addAll(world.getPlayers());
		}

		for (Expression<?> message : getMessages()) {
			if (message instanceof VariableString) {
				if (!dispatchEvent(getRawString(event, (VariableString) message), receivers))
					continue;
				BaseComponent[] components = BungeeConverter.convert(((VariableString) message).getMessageComponents(event));
				receivers.forEach(receiver -> receiver.spigot().sendMessage(components));
			} else if (message instanceof ExprColoured && ((ExprColoured) message).isUnsafeFormat()) { // Manually marked as trusted
				for (Object realMessage : message.getArray(event)) {
					if (!dispatchEvent(Utils.replaceChatStyles((String) realMessage), receivers))
						continue;
					BaseComponent[] components = BungeeConverter.convert(ChatMessages.parse((String) realMessage));
					receivers.forEach(receiver -> receiver.spigot().sendMessage(components));
				}
			} else {
				for (Object messageObject : message.getArray(event)) {
					String realMessage = messageObject instanceof String ? (String) messageObject : Classes.toString(messageObject);
					if (!dispatchEvent(Utils.replaceChatStyles(realMessage), receivers))
						continue;
					receivers.forEach(receiver -> receiver.sendMessage(realMessage));
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "broadcast " + messageExpr.toString(event, debug) + (worlds == null ? "" : " to " + worlds.toString(event, debug));
	}

	private Expression<?>[] getMessages() {
		if (messageExpr instanceof ExpressionList && !messageExpr.getAnd()) {
			return new Expression[] {CollectionUtils.getRandom(messages)};
		}
		return messages;
	}

	/**
	 * Manually calls a {@link BroadcastMessageEvent}.
	 * @param message the message
	 * @return true if the dispatched event does not get cancelled
	 */
	@SuppressWarnings("deprecation")
	private static boolean dispatchEvent(String message, List<CommandSender> receivers) {
		Set<CommandSender> recipients = Collections.unmodifiableSet(new HashSet<>(receivers));
		BroadcastMessageEvent broadcastEvent;
		if (Skript.isRunningMinecraft(1, 14)) {
			broadcastEvent = new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), message, recipients);
		} else {
			broadcastEvent = new BroadcastMessageEvent(message, recipients);
		}
		Bukkit.getPluginManager().callEvent(broadcastEvent);
		return !broadcastEvent.isCancelled();
	}

	@Nullable
	private static String getRawString(Event event, Expression<? extends String> string) {
		if (string instanceof VariableString)
			return ((VariableString) string).toUnformattedString(event);
		String rawString = string.getSingle(event);
		rawString = SkriptColor.replaceColorChar(rawString);
		if (rawString.toLowerCase().contains("&x")) {
			rawString = StringUtils.replaceAll(rawString, HEX_PATTERN, matchResult ->
				"<#" + matchResult.group(1).replace("&", "") + '>');
		}
		return rawString;
	}

}
