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

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
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

@Name("Server Icon")
@Description({"Icon of the server in the server list. Can be set to an icon that loaded using the",
		"<a href='effects.html#EffLoadServerIcon'>load server icon</a> effect,",
		"or can be reset to the default icon in a <a href='events.html#server_list_ping'>server list ping</a>.",
		"'default server icon' returns the default server icon (server-icon.png) always and cannot be changed.",})
@Examples({"on script load:",
		"	set {server-icons::default} to the default server icon"})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
public class ExprServerIcon extends SimpleExpression<CachedServerIcon> {

	static {
		Skript.registerExpression(ExprServerIcon.class, CachedServerIcon.class, ExpressionType.PROPERTY,
				"[the] [(1¦(default)|2¦(shown|sent))] [server] icon");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	private boolean isServerPingEvent, isDefault;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The server icon expression requires Paper 1.12.2 or newer");
			return false;
		}
		isServerPingEvent = getParser().isCurrentEvent(PaperServerListPingEvent.class);
		isDefault = (parseResult.mark == 0 && !isServerPingEvent) || parseResult.mark == 1;
		if (!isServerPingEvent && !isDefault) {
			Skript.error("The 'shown' server icon expression can't be used outside of a server list ping event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public CachedServerIcon[] get(Event e) {
		CachedServerIcon icon = null;
		if ((isServerPingEvent && !isDefault) && PAPER_EVENT_EXISTS)
			icon = ((PaperServerListPingEvent) e).getServerIcon();
		else
			icon = Bukkit.getServerIcon();
		if (icon == null || icon.getData() == null)
			return null;
		return CollectionUtils.array(icon);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (isServerPingEvent && !isDefault) {
			if (getParser().getHasDelayBefore().isTrue()) {
				Skript.error("Can't change the server icon anymore after the server list ping event has already passed");
				return null;
			}
			if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
				return CollectionUtils.array(CachedServerIcon.class);
		}
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		PaperServerListPingEvent event = (PaperServerListPingEvent) e;
		switch (mode) {
			case SET:
				event.setServerIcon((CachedServerIcon) delta[0]);
				break;
			case RESET:
				event.setServerIcon(Bukkit.getServerIcon());
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends CachedServerIcon> getReturnType() {
		return CachedServerIcon.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the " + (!isServerPingEvent || isDefault ? "default" : "shown") + " server icon";
	}

}