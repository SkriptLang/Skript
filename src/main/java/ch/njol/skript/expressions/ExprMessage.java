
/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import org.bukkit.event.Event;
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
import ch.njol.skript.doc.*;
import ch.njol.skript.events.util.PlayerChatEventHandler;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.command.UnknownCommandEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
@Name("Message")
@Description("The (chat) message of a chat event, the join message of a join event, the quit message of a quit event, or the death message on a death event. This expression is mostly useful for being changed.")
@Examples({"on chat:",
	"\tplayer has permission \"admin\"",
	"\tset message to \"<red>%message%\"",
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
	"on death:",
	"\tset the death message to \"%player% died!\"",
	"",
	"on unknown command: #requires Paper",
	"\tset unknown command message to \"The command \"\"%command%\"\" doesn't exist.\""})
@Since("1.4.6 (chat message), 1.4.9 (join & quit messages), 2.0 (death message), INSERT VERSION (unknown command message)")
@Events({"chat", "join", "quit", "death", "unknown command"})
public class ExprMessage extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprMessage.class, String.class, ExpressionType.SIMPLE, MessageType.patterns.toArray(new String[0]));
	}

	@SuppressWarnings("null")
	private MessageType type;

	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		type = (MessageType) MessageType.supportedTypes.toArray()[matchedPattern];
		if (!ScriptLoader.isCurrentEvent(type.getEvents())) {
			Skript.error("The " + type.name + " message can only be used in a " + type.name + " event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	protected String[] get(final Event e) {
		return new String[]{type.get(e)};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (delta == null)
			return;
		type.set(e, "" + delta[0]);
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
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + type.name + " message";
	}

	@SuppressWarnings("unchecked")
	private static enum MessageType {
		CHAT("chat", "[chat( |-)]message", true) {
			@Override
			Class<? extends Event>[] getEvents() {
				return new Class[]{PlayerChatEventHandler.usesAsyncEvent ? AsyncPlayerChatEvent.class : PlayerChatEvent.class};
			}

			@Override
			@Nullable
			String get(final Event e) {
				if (PlayerChatEventHandler.usesAsyncEvent)
					return ((AsyncPlayerChatEvent) e).getMessage();
				else
					return ((PlayerChatEvent) e).getMessage();
			}

			@Override
			void set(final Event e, final String message) {
				if (PlayerChatEventHandler.usesAsyncEvent)
					((AsyncPlayerChatEvent) e).setMessage(message);
				else
					((PlayerChatEvent) e).setMessage(message);
			}
		},
		JOIN("join", "(join|log[ ]in)( |-)message", true) {
			@Override
			Class<? extends Event>[] getEvents() {
				return new Class[]{PlayerJoinEvent.class};
			}

			@Override
			@Nullable
			String get(final Event e) {
				return ((PlayerJoinEvent) e).getJoinMessage();
			}

			@Override
			void set(final Event e, final String message) {
				((PlayerJoinEvent) e).setJoinMessage(message);
			}
		},
		QUIT("quit", "(quit|leave|log[ ]out|kick)( |-)message", true) {
			@Override
			Class<? extends Event>[] getEvents() {
				return new Class[]{PlayerQuitEvent.class, PlayerKickEvent.class};
			}

			@Override
			@Nullable
			String get(final Event e) {
				if (e instanceof PlayerKickEvent)
					return ((PlayerKickEvent) e).getLeaveMessage();
				else
					return ((PlayerQuitEvent) e).getQuitMessage();
			}

			@Override
			void set(final Event e, final String message) {
				if (e instanceof PlayerKickEvent)
					((PlayerKickEvent) e).setLeaveMessage(message);
				else
					((PlayerQuitEvent) e).setQuitMessage(message);
			}
		},
		DEATH("death", "death( |-)message", true) {
			@Override
			Class<? extends Event>[] getEvents() {
				return new Class[]{EntityDeathEvent.class};
			}

			@Override
			@Nullable
			String get(final Event e) {
				if (e instanceof PlayerDeathEvent)
					return ((PlayerDeathEvent) e).getDeathMessage();
				return null;
			}

			@Override
			void set(Event e, String message) {

			}

		},
		UNKNOWN_COMMAND("unknown command", "unknown (command|cmd) message", Skript.classExists("org.bukkit.event.command.UnknownCommandEvent")) {
			@Override
			Class<? extends Event>[] getEvents() {
				return new Class[]{UnknownCommandEvent.class};
			}

			@Nullable
			@Override
			String get(Event e) {
				return ((UnknownCommandEvent) e).getMessage();
			}

			@Override
			void set(Event e, String message) {
				((UnknownCommandEvent) e).setMessage(message);
			}
		};

		static ArrayList<String> patterns = new ArrayList<>();
		static EnumSet<MessageType> supportedTypes;

		static {
			MessageType[] types = values();
			supportedTypes = EnumSet.noneOf(MessageType.class);
			for (int i = 0; i < types.length; i++) {
				if (types[i].supported) {
					patterns.add(types[i].pattern);
					supportedTypes.add(types[i]);
				}
			}
		}

		final String name;
		private final String pattern;
		private final boolean supported;

		MessageType(final String name, final String pattern, boolean supported) {
			this.name = name;
			this.pattern = "[the] " + pattern;
			this.supported = supported;
		}

		abstract Class<? extends Event>[] getEvents();

		@Nullable
		abstract String get(Event e);

		abstract void set(Event e, String message);
	}

}