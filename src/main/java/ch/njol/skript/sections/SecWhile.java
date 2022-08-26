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
package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class SecWhile extends LoopSection {

	static {
		Skript.registerSection(SecWhile.class, "[(:do)] while <.+>");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Condition condition;

	@Nullable
	private TriggerItem actualNext;

	private boolean doWhile;
	private boolean ranDoWhile = false;
	private final transient Map<Event, Long> currentLoopCounter = new WeakHashMap<>();

	@Override
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						SectionNode sectionNode,
						List<TriggerItem> triggerItems) {
		String expr = parseResult.regexes.get(0).group();

		condition = Condition.parse(expr, "Can't understand this condition: " + expr);
		if (condition == null)
			return false;

		doWhile = parseResult.hasTag("do");
		loadOptionalCode(sectionNode);
		super.setNext(this);
		return true;
	}

	@Nullable
	@Override
	protected TriggerItem walk(Event event) {
		if ((doWhile && !ranDoWhile) || condition.check(event)) {
			ranDoWhile = true;
			currentLoopCounter.put(event, (currentLoopCounter.getOrDefault(event, 0L)) + 1);
			return walk(event, true);
		} else {
			exit(event);
			debug(event, false);
			return actualNext;
		}
	}

	@Override
	public SecWhile setNext(@Nullable TriggerItem next) {
		actualNext = next;
		return this;
	}

	@Nullable
	public TriggerItem getActualNext() {
		return actualNext;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (doWhile ? "do " : "") + "while " + condition.toString(event, debug);
	}

	@Override
	public void exit(Event event) {
		ranDoWhile = false;
		currentLoopCounter.remove(event);
	}

	@Override
	public long getLoopCounter(Event event) {
		return currentLoopCounter.getOrDefault(event, 1L);
	}

}
