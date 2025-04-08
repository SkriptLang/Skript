package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
@Name("Message")
@Description(
	"The (chat) message of a chat event, the join message of a join event, the quit message of a quit event, " +
	"the death message of a death event or the broadcasted message in a broadcast event. " +
	"This expression is mostly useful for being changed."
)
@Examples({
		"on chat:",
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
			"\tif {vanish::%player's uuid%} is set:",
				"\t\tclear quit message",
			"\telse:",
				"\t\tset quit message to \"%player% left this awesome server!\"",
		"",
		"on death:",
			"\tset the death message to \"%player% died!\"",
		"",
		"on broadcast:",
			"\tset broadcast message to \"&a[BROADCAST] %broadcast message%\""
})
@Since("1.4.6 (chat message), 1.4.9 (join & quit messages), 2.0 (death message), 2.9.0 (clear message), 2.10 (broadcasted message)")
@Events({"chat", "join", "quit", "death", "broadcast"})
public class ExprMessage extends SimpleExpression<String> {
	
	@SuppressWarnings("unchecked")
	private static enum MessageType {
		CHAT("chat", "[chat( |-)]message", AsyncPlayerChatEvent.class) {
			@Override
			String get(Event event) {
				return ((AsyncPlayerChatEvent) event).getMessage();
			}
			
			@Override
			void set(Event event, String message) {
				((AsyncPlayerChatEvent) event).setMessage(message);
			}
		},
		JOIN("join", "(join|log[ ]in)( |-)message", PlayerJoinEvent.class) {
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
		QUIT("quit", "(quit|leave|log[ ]out|kick)( |-)message", PlayerQuitEvent.class, PlayerKickEvent.class) {
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
		DEATH("death", "death( |-)message", EntityDeathEvent.class) {
			@Override
			@Nullable
			String get(final Event e) {
				if (e instanceof PlayerDeathEvent)
					return ((PlayerDeathEvent) e).getDeathMessage();
				return null;
			}
			
			@Override
			void set(final Event e, final String message) {
				if (e instanceof PlayerDeathEvent)
					((PlayerDeathEvent) e).setDeathMessage(message);
			}
		},
		BROADCAST("broadcast", "broadcast(-|[ed] )message", BroadcastMessageEvent.class) {
			@Override
			@Nullable String get(Event event) {
				if (event instanceof BroadcastMessageEvent broadcastMessageEvent)
					return broadcastMessageEvent.getMessage();
				return null;
			}

			@Override
			void set(Event event, String message) {
				if (event instanceof BroadcastMessageEvent broadcastMessageEvent)
					broadcastMessageEvent.setMessage(message);
			}
		};
		
		final String name;
		private final String pattern;
		final Class<? extends Event>[] events;
		
		MessageType(final String name, final String pattern, final Class<? extends Event>... events) {
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
		abstract String get(Event e);
		
		abstract void set(Event e, String message);
		
	}
	
	static {
		Skript.registerExpression(ExprMessage.class, String.class, ExpressionType.SIMPLE, MessageType.patterns);
	}
	
	@SuppressWarnings("null")
	private MessageType type;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		type = MessageType.values()[matchedPattern];
		if (!getParser().isCurrentEvent(type.events)) {
			Skript.error("The " + type.name + " message can only be used in a " + type.name + " event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
		for (final Class<? extends Event> c : type.events) {
			if (c.isInstance(e))
				return new String[] {type.get(e)};
		}
		return new String[0];
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(String.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert mode == ChangeMode.SET || mode == ChangeMode.DELETE;
		for (final Class<? extends Event> c : type.events) {
			if (c.isInstance(e)) {
				type.set(e, (mode == ChangeMode.DELETE) ? "" : delta[0].toString());
			}
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
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + type.name + " message";
	}
	
}
