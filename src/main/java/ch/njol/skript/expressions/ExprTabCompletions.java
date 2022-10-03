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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.util.StringUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExprTabCompletions extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprTabCompletions.class, String.class, ExpressionType.SIMPLE, "tab completions [(of|for) position %-number%]");
	}

	@Nullable
	private Expression<Number> position;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(TabCompleteEvent.class)) {
			Skript.error("Tab completions are only usable in a tab complete event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		position = (Expression<Number>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		return ((TabCompleteEvent) event).getCompletions().toArray(new String[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Object[].class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		TabCompleteEvent tabEvent = (TabCompleteEvent) event;

		Number number = this.position.getSingle(event);
		int position = 1;
		if (number != null) {
			position = number.intValue();
		}
		switch (mode) {
			case ADD:
			case SET:
				String buff = tabEvent.getBuffer();
				String[] buffers = buff.split(" ");
				String last = buff.substring(buff.length() - 1);
				if ((position == buffers.length && last.equalsIgnoreCase(" ")) ||
					(position + 1 == buffers.length && !last.equalsIgnoreCase(" "))) {
					String arg;
					if (position == buffers.length) {
						arg = "";
					} else {
						arg = buffers[position];
					}

					List<String> completions = mode == ChangeMode.SET ? new ArrayList<>() : new ArrayList<>(tabEvent.getCompletions());
					if (delta == null) {
						if (mode == ChangeMode.SET) {
							tabEvent.setCompletions(Collections.singletonList(""));
						}
						return;
					}
					for (Object o : delta) {
						String object = Classes.toString(o);
						if (StringUtil.startsWithIgnoreCase(object, arg)) {
							completions.add(object);
						}
					}
					tabEvent.setCompletions(completions);
				}
				break;
			case REMOVE:
				assert delta != null;
				for (Object object : delta) {
					tabEvent.getCompletions().remove(object.toString());
				}
				break;
			case DELETE:
			case REMOVE_ALL:
				tabEvent.setCompletions(Collections.singletonList(""));
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String position = this.position == null ? "" : " for position " + this.position.toString(event, debug);
		return "tab completions" + position;
	}
}
