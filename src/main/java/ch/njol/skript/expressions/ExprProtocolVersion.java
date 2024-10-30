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
import org.jetbrains.annotations.Nullable;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Protocol Version")
@Description({"The protocol version that will be sent as the protocol version of the server in a server list ping event. " +
		"For more information and list of protocol versions <a href='https://wiki.vg/Protocol_version_numbers'>visit wiki.vg</a>.",
		"If this protocol version doesn't match with the protocol version of the client, the client will see the <a href='#ExprVersionString'>version string</a>.",
		"But please note that, this expression has no visual effect over the version string. " +
		"For example if the server uses PaperSpigot 1.12.2, and you make the protocol version 107 (1.9),",
		"the version string will not be \"Paper 1.9\", it will still be \"Paper 1.12.2\".",
		"But then you can customize the <a href='#ExprVersionString'>version string</a> as you wish.",
		"Also if the protocol version of the player is higher than protocol version of the server, it will say",
		"\"Server out of date!\", and if vice-versa \"Client out of date!\" when you hover on the ping bars.",
		"",
		"This can be set in a <a href='events.html#server_list_ping'>server list ping</a> event only",
		"(increase and decrease effects cannot be used because that wouldn't make sense).",})
@Examples({"on server list ping:",
		"\tset the version string to \"&lt;light green&gt;Version: &lt;orange&gt;%minecraft version%\"",
		"\tset the protocol version to 0 # 13w41a (1.7) - so the player will see the custom version string almost always"})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
@Events("server list ping")
public class ExprProtocolVersion extends SimpleExpression<Long> {

	static {
		Skript.registerExpression(ExprProtocolVersion.class, Long.class, ExpressionType.SIMPLE, "[the] [server] [(sent|required|fake)] protocol version [number]");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The protocol version expression requires Paper 1.12.2 or newer");
			return false;
		} else if (!getParser().isCurrentEvent(PaperServerListPingEvent.class)) {
			Skript.error("The protocol version expression can't be used outside of a server list ping event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public Long[] get(Event e) {
		if (!(e instanceof PaperServerListPingEvent))
			return null;

		return CollectionUtils.array((long) ((PaperServerListPingEvent) e).getProtocolVersion());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Can't change the protocol version anymore after the server list ping event has already passed");
			return null;
		}
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (!(e instanceof PaperServerListPingEvent))
			return;

		((PaperServerListPingEvent) e).setProtocolVersion(((Number) delta[0]).intValue());
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the protocol version";
	}

}