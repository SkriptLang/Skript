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
import org.bukkit.event.server.ServerListPingEvent;
import org.eclipse.jdt.annotation.Nullable;

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

@Name("Version String")
@Description({"The text to show if the protocol version of the server doesn't match with protocol version of the client. " +
		"You can check the <a href='#ExprProtocolVersion'>protocol version</a> expression for more information about this.",
		"This can only be set in a <a href='events.html#server_list_ping'>server list ping</a> event."})
@Examples({"on server list ping:",
		"	set the protocol version to 0 # 13w41a (1.7), so it will show the version string always",
		"	set the version string to \"&lt;light green&gt;Version: &lt;orange&gt;%minecraft version%\""})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
@Events("server list ping")
public class ExprVersionString extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprVersionString.class, String.class, ExpressionType.SIMPLE, "[the] [(shown|custom)] version [(string|text)]");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(ServerListPingEvent.class)) {
			Skript.error("The version string expression requires Paper 1.12.2 or newer");
			return false;
		} else if (!(PAPER_EVENT_EXISTS && getParser().isCurrentEvent(PaperServerListPingEvent.class))) {
			Skript.error("The version string expression can't be used outside of a server list ping event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public String[] get(Event e) {
		return CollectionUtils.array(((PaperServerListPingEvent) e).getVersion());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Can't change the version string anymore after the server list ping event has already passed");
			return null;
		}
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(String.class);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		((PaperServerListPingEvent) e).setVersion(((String) delta[0]));
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
	public String toString(@Nullable Event e, boolean debug) {
		return "the version string";
	}

}