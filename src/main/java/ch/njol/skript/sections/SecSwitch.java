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
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.experiment.Feature;
import org.skriptlang.skript.lang.util.ContextLocal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Name("Switch Conditions (Experimental)")
@Description("Tests an input against several conditions, running only the sections that match.")
@Since("INSERT VERSION")
public class SecSwitch extends LoopSection {

	protected static final ContextLocal<Event, Object> currentSwitchEntry = new ContextLocal<>();

	static {
		Skript.registerSection(SecSwitch.class, "switch %objects%");
	}

	private @UnknownNullability TriggerItem actualNext;
	private @UnknownNullability Expression<?> expression;
	private final List<EffSecSwitchCase> cases = new ArrayList<>();
	private final ContextLocal<Event, Iterator<?>> iterator = new ContextLocal<>(this::makeIterator);

	public static @Nullable Object current(Event event) {
		return currentSwitchEntry.get(event);
	}

	@Override
	public boolean init(Expression<?>[] expressions,
						int pattern,
						Kleenean isDelayed,
						ParseResult result,
						SectionNode node,
						List<TriggerItem> items) {
		if (!this.hasExperiment(Feature.SWITCH_SECTIONS))
			return false;
		this.expression = LiteralUtils.defendExpression(expressions[0]);
		if (!LiteralUtils.canInitSafely(expression)) {
			Skript.error("Can't understand this switch section: '" + result.expr.substring(7) + "'");
			return false;
		}
		try {
			this.loadOptionalCode(node);
		} catch (IllegalSyntaxError error) {
			Skript.error("Illegal syntax in switch case: '" + error.getItem() + "'");
			return false;
		}
		super.setNext(this);
		return true;
	}

	@Override
	protected void setTriggerItems(List<TriggerItem> items) {
		this.cases.clear();
		for (TriggerItem item : items) {
			if (item instanceof MetaSyntaxElement) {
				continue;
			} else if (item instanceof EffSecSwitchCase) {
				this.cases.add((EffSecSwitchCase) item);
				continue;
			} else if (item instanceof EffectSectionEffect
				&& ((EffectSectionEffect) item).getEffectSection() instanceof EffSecSwitchCase){
				this.cases.add(((EffSecSwitchCase) ((EffectSectionEffect) item).getEffectSection()));
				continue;
			}
			throw new IllegalSyntaxError(item);
		}
		super.setTriggerItems(items);
	}

	@Override
	public SecSwitch setNext(@Nullable TriggerItem next) {
		this.actualNext = next;
		return this;
	}

	private @Nullable Object outerSwitch;

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		final Iterator<?> iterator = this.iterator.get(event);
		if (iterator == null || !iterator.hasNext()) {
			exit(event);
			debug(event, false);
			return actualNext;
		} else {
			Object next = iterator.next();
			this.outerSwitch = SecSwitch.currentSwitchEntry.get(event);
			SecSwitch.currentSwitchEntry.set(event, next);
			return this.walk(event, true);
		}
	}

	@Override
	public TriggerItem getActualNext() {
		return actualNext;
	}

	@Override
	public void exit(Event event) {
		this.iterator.remove(event);
		SecSwitch.currentSwitchEntry.remove(event);
		if (outerSwitch != null)
			SecSwitch.currentSwitchEntry.set(event, outerSwitch);
		super.exit(event);
	}

	private Iterator<?> makeIterator(Event event) {
		return Arrays.stream(expression.getArray(event)).iterator();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "switch " + expression.toString(event, debug);
	}

	private static class IllegalSyntaxError extends Error {
		private final TriggerItem item;

		private IllegalSyntaxError(TriggerItem item) {
			this.item = item;
		}

		public TriggerItem getItem() {
			return item;
		}

	}

	public static boolean isInSwitch(SyntaxElement item) {
		List<TriggerSection> sections = item.getParser().getCurrentSections();
		if (sections.isEmpty())
			return false;
		return sections.get(sections.size() - 1) instanceof SecSwitch;
	}

	public static boolean isInSwitchCase(SyntaxElement item) {
		List<TriggerSection> sections = item.getParser().getCurrentSections();
		if (sections.isEmpty())
			return false;
		return sections.get(sections.size() - 1) instanceof EffSecSwitchCase;
	}

	static SecSwitch getSwitch(SyntaxElement item) {
		List<TriggerSection> sections = item.getParser().getCurrentSections();
		if (sections.isEmpty())
			throw new IllegalStateException();
		return (SecSwitch) sections.get(sections.size() - 1);
	}

}
