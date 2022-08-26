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
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.ContainerExpression;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.Container.ContainerType;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class SecLoop extends LoopSection {

	static {
		Skript.registerSection(SecLoop.class, "loop %objects%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> expr;

	private final transient Map<Event, Object> current = new WeakHashMap<>();
	private final transient Map<Event, Iterator<?>> currentIter = new WeakHashMap<>();
	private final transient Map<Event, Long> currentLoopCounter = new WeakHashMap<>();

	@Nullable
	private TriggerItem actualNext;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						SectionNode sectionNode,
						List<TriggerItem> triggerItems) {
		expr = LiteralUtils.defendExpression(exprs[0]);
		if (!LiteralUtils.canInitSafely(expr)) {
			Skript.error("Can't understand this loop: '" + parseResult.expr.substring(5) + "'");
			return false;
		}

		if (Container.class.isAssignableFrom(expr.getReturnType())) {
			ContainerType type = expr.getReturnType().getAnnotation(ContainerType.class);
			if (type == null)
				throw new SkriptAPIException(expr.getReturnType().getName() + " implements Container but is missing the required @ContainerType annotation");
			expr = new ContainerExpression((Expression<? extends Container<?>>) expr, type.value());
		}

		if (expr.isSingle()) {
			Skript.error("Can't loop " + expr + " because it's only a single value");
			return false;
		}

		loadOptionalCode(sectionNode);
		super.setNext(this);

		return true;
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event event) {
		Iterator<?> iter = currentIter.get(event);
		if (iter == null) {
			iter = expr instanceof Variable ? ((Variable<?>) expr).variablesIterator(event) : expr.iterator(event);
			if (iter != null) {
				if (iter.hasNext())
					currentIter.put(event, iter);
				else
					iter = null;
			}
		}
		if (iter == null || !iter.hasNext()) {
			exit(event);
			debug(event, false);
			return actualNext;
		} else {
			current.put(event, iter.next());
			currentLoopCounter.put(event, (currentLoopCounter.getOrDefault(event, 0L)) + 1);
			return walk(event, true);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "loop " + expr.toString(event, debug);
	}

	@Nullable
	public Object getCurrent(Event event) {
		return current.get(event);
	}

	public Expression<?> getLoopedExpression() {
		return expr;
	}

	@Override
	public SecLoop setNext(@Nullable TriggerItem next) {
		actualNext = next;
		return this;
	}

	@Nullable
	@Override
	public TriggerItem getActualNext() {
		return actualNext;
	}

	@Override
	public void exit(Event event) {
		current.remove(event);
		currentIter.remove(event);
		currentLoopCounter.remove(event);
	}

	@Override
	public long getLoopCounter(Event event) {
		return currentLoopCounter.getOrDefault(event, 1L);
	}

}
