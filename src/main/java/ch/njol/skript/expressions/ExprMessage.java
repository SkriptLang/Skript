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

import org.bukkit.event.Event;
import org.bukkit.event.command.UnknownCommandEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.util.PlayerChatEventHandler;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@SuppressWarnings("deprecation")
@Name("Message")
@Description("The (chat) message of a chat event, the join message of a join event, the quit message of a quit event, or the death message on a death event. This expression is mostly useful for being changed.")
@Examples({"on chat:",
		"\tplayer has permission \"admin\"",
		"\tset message to \"&c%message%\"",
		"",
		"on first join:",
		"\tset join message to \"Welcome %player% to our awesome server!\"",
		"",
		"on join:",
		"\tplayer has played before",
		"\tset join message to \"Welcome back, %player%!\"",
		"",
		"on quit:",
		"\tset quit message to \"%player% left this awesome server!\"",
		"",
		"on kick:",
		"\tset kick message to \"You've been kicked from the server!\"",
		"",
		"on death:",
		"\tset the death message to \"%player% died!\"",
		"",
		"on unknown command:",
		"\tset unknown command message to \"<##ff3737>%command% is not a valid command!\""
})
@Since("1.4.6 (chat message), 1.4.9 (join & quit messages), 2.0 (death message), INSERT VERSION (unknown command message)")
@Events({"chat", "join", "quit", "death", "unknown command"})
public class ExprMessage extends SimpleExpression<String> {
	
	@SuppressWarnings("unchecked")
	private static enum MessageType {
		CHAT("chat", "[chat( |-)]message", PlayerChatEventHandler.usesAsyncEvent ? AsyncPlayerChatEvent.class : PlayerChatEvent.class) {
			@Override
			@Nullable
			String get(Event event) {
				if (PlayerChatEventHandler.usesAsyncEvent)
					return ((AsyncPlayerChatEvent) event).getMessage();
				else
					return ((PlayerChatEvent) event).getMessage();
			}
			
			@Override
			void set(Event event, String message) {
				if (PlayerChatEventHandler.usesAsyncEvent)
					((AsyncPlayerChatEvent) event).setMessage(message);
				else
					((PlayerChatEvent) event).setMessage(message);
			}
		},
		JOIN("join", "(join|log[ ]in)( |-)message", PlayerJoinEvent.class) {
			@Override
			@Nullable
			String get(Event event) {
				return ((PlayerJoinEvent) event).getJoinMessage();
			}
			
			@Override
			void set(Event event, String message) {
				((PlayerJoinEvent) event).setJoinMessage(message);
			}
		},
		QUIT("quit", "(quit|leave|log[ ]out|kick)( |-)message", PlayerQuitEvent.class, PlayerKickEvent.class) {
			@Override
			@Nullable
			String get(Event event) {
				if (event instanceof PlayerKickEvent)
					return ((PlayerKickEvent) event).getLeaveMessage();
				else
					return ((PlayerQuitEvent) event).getQuitMessage();
			}
			
			@Override
			void set(Event event, String message) {
				if (event instanceof PlayerKickEvent)
					((PlayerKickEvent) event).setLeaveMessage(message);
				else
					((PlayerQuitEvent) event).setQuitMessage(message);
			}
		},
		DEATH("death", "death( |-)message", EntityDeathEvent.class) {
			@Override
			@Nullable
			String get(Event event) {
				if (event instanceof PlayerDeathEvent)
					return ((PlayerDeathEvent) event).getDeathMessage();
				return null;
			}
			
			@Override
			void set(Event event, String message) {
				if (event instanceof PlayerDeathEvent)
					((PlayerDeathEvent) event).setDeathMessage(message);
			}
		},
		UNKNOWN_CMD("unknown command", "unknown command( |-)message", UnknownCommandEvent.class) {
			@Override
			@Nullable String get(Event event) {
				return ((UnknownCommandEvent) event).getMessage();
			}

			@Override
			void set(Event event, String message) {
				((UnknownCommandEvent) event).setMessage(message);
			}
		};
		
		String name;
		private String pattern;
		Class<? extends Event>[] events;
		
		MessageType(String name, String pattern, Class<? extends Event>... events) {
			this.name = name;
			this.pattern = "[the] " + pattern;
			this.events = events;
		}
		
		static String[] patterns;
		static {
			patterns = new String[values().length];
			for (int i = 0; i < patterns.length; i++)
				patterns[i] = values()[i].pattern;
		}
		
		@Nullable
		abstract String get(Event event);
		
		abstract void set(Event event, String message);
		
	}
	
	static {
		Skript.registerExpression(ExprMessage.class, String.class, ExpressionType.SIMPLE, MessageType.patterns);
	}
	
	@SuppressWarnings("null")
	private MessageType type;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = MessageType.values()[matchedPattern];
		if (!getParser().isCurrentEvent(type.events)) {
			Skript.error("The " + type.name + " message can only be used in a " + type.name + " event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
	
	@Override
	protected String[] get(Event event) {
		for (Class<? extends Event> c : type.events) {
			if (c.isInstance(event))
				return new String[] {type.get(event)};
		}
		return new String[0];
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(String.class);
		return null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert mode == ChangeMode.SET;
		assert delta != null;
		for (Class<? extends Event> c : type.events) {
			if (c.isInstance(event))
				type.set(event, "" + delta[0]);
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + type.name + " message";
	}
	
}
