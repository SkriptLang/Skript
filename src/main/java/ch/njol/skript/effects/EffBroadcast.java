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
import ch.njol.skript.expressions.ExprColoured;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Broadcast")
@Description("Broadcasts a message to the server.")
@Examples({
	"broadcast \"Welcome %player% to the server!\"",
	"broadcast \"Woah! It's a message!\""
})
@Since("1.0, 2.6 (broadcasting objects), 2.6.1 (using advanced formatting)")
public class EffBroadcast extends Effect {

	static {
		Skript.registerEffect(EffBroadcast.class, "broadcast %objects% [(to|in) %-worlds%]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> messageExpr;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?>[] messages;
	@Nullable
	private Expression<World> worlds;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		messageExpr = LiteralUtils.defendExpression(exprs[0]);
		messages = messageExpr instanceof ExpressionList ?
			((ExpressionList<?>) messageExpr).getExpressions() : new Expression[] {messageExpr};
		worlds = (Expression<World>) exprs[1];
		return LiteralUtils.canInitSafely(messageExpr);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void execute(Event e) {
		List<CommandSender> receivers = new ArrayList<>();
		if (worlds == null) {
			receivers.addAll(Bukkit.getOnlinePlayers());
			receivers.add(Bukkit.getConsoleSender());
		} else {
			for (World world : worlds.getArray(e))
				receivers.addAll(world.getPlayers());
		}

		for (Expression<?> message : getMessages()) {
			if (message instanceof VariableString) {
				BaseComponent[] components = BungeeConverter.convert(((VariableString) message).getMessageComponents(e));
				receivers.forEach(receiver -> receiver.spigot().sendMessage(components));
			} else if (message instanceof ExprColoured && ((ExprColoured) message).isUnsafeFormat()) { // Manually marked as trusted
				for (Object realMessage : message.getArray(e)) {
					BaseComponent[] components = BungeeConverter.convert(ChatMessages.parse((String) realMessage));
					receivers.forEach(receiver -> receiver.spigot().sendMessage(components));
				}
			} else {
				for (Object messageObject : message.getArray(e)) {
					String realMessage = messageObject instanceof String ? (String) messageObject : Classes.toString(messageObject);
					receivers.forEach(receiver -> receiver.sendMessage(realMessage));
				}
			}
		}
	}

	private Expression<?>[] getMessages() {
		if (messageExpr instanceof ExpressionList && !messageExpr.getAnd()) {
			return new Expression[] {CollectionUtils.getRandom(messages)};
		}
		return messages;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "broadcast " + messageExpr.toString(e, debug) + (worlds == null ? "" : " to " + worlds.toString(e, debug));
	}
	
}
