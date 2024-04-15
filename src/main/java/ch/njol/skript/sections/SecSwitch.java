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
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.experiment.Feature;
import org.skriptlang.skript.lang.util.ContextLocal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Name("Switch Conditions (Experimental)")
@Description({
	"Tests an input (or inputs) against several conditions, running ALL the conditions that match.",
	"If multiple inputs like a list are used, the switch section will be repeated for each value.",
	"Switch blocks are a kind of loop, and so can be continued and exited from.",
	"By default, switches run every matching section.",
	"To avoid this happening, you can use the loop `continue` and `exit` effects.",
	"The subject of the switch can be referred to with `(it|this)` inside conditions.",
	"",
	"For advanced users, switches come with two alternate modes: 'strict' and 'fall-through'.",
	"This behaviour can be confusing so they are not enabled by default.",
	"In STRICT mode (enabled with `@strict switch mode`):",
	"\t- The switch block will continue after ANY case matches.",
	"In FALL-THROUGH mode (enabled with `@fall-through switch mode`):",
	"\t- The first matching case will be run.",
	"\t- EVERY subsequent case will also be run, regardless of the condition.",
	"\t- This mimics Java's 'switch' behaviour, i.e. you need to continue/exit after a section."
})
@Examples({
	"using switch sections",
	"on swap hand items:",
	"\tswitch player's tool:",
	"\t\tit is any sword:",
	"\t\t\tbroadcast \"it's a sword!\"",
	"\t\tit is a diamond sword:",
	"\t\t\tbroadcast \"it's an expensive sword!\"",
	"\t\tit exists:",
	"\t\t\tbroadcast \"it's a thing!\"",
	"\t\tit is alive:",
	"\t\t\tbroadcast \"tools aren't alive, silly\"",
	"",
	"check {_numbers::*}:",
	"\tif it is 3:",
	"\t\tbroadcast \"it's 3!\"",
	"\t\tcontinue",
	"\tif it is less than 10:",
	"\t\tbroadcast \"it's not 11!\""
})
@Since("INSERT VERSION")
public class SecSwitch extends LoopSection {

	protected static final ContextLocal<Event, Object> currentSwitchEntry = new ContextLocal<>();
	protected static final SkriptPattern STRICT = PatternCompiler.compile("strict switch [mode|cases]"),
		FALL_THROUGH = PatternCompiler.compile("fall[ |-]through switch [mode|cases]");

	static {
		Skript.registerSection(SecSwitch.class, "switch %objects%", "check %objects%");
	}

	private @NotNull Mode mode = Mode.NORMAL;
	private @UnknownNullability TriggerItem actualNext;
	private @UnknownNullability Expression<?> expression;
	private final List<EffSecSwitchCase> cases = new ArrayList<>();
	private final ContextLocal<Event, Iterator<?>> iterator = new ContextLocal<>(this::makeIterator);
	private final ContextLocal<Event, Boolean> hasPassed = new ContextLocal<>(Boolean.FALSE::booleanValue);

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
		this.mode = getSwitchMode(ParserInstance.get(), mode);
		try {
			this.loadOptionalCode(node);
		} catch (IllegalSyntaxError error) {
			Skript.error("Illegal syntax in switch case: '" + error.getItem() + "'");
			return false;
		}
		super.setNext(this);
		return true;
	}

	protected static Mode getSwitchMode(ParserInstance parser, Mode defaultMode) {
		if (parser.hasAnnotationMatching(STRICT))
			return Mode.STRICT;
		else if (parser.hasAnnotationMatching(FALL_THROUGH))
			return Mode.FALL_THROUGH;
		return defaultMode;
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

	// storing what the world was like when we entered this switch
	// e.g. for switch inside switch
	private @Nullable Object outerSwitch;
	private boolean outerPassed;

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		final Iterator<?> iterator = this.iterator.get(event);
		if (iterator == null || !iterator.hasNext()) {
			exit(event);
			debug(event, false);
			return actualNext;
		} else {
			Object next = iterator.next();
			this.outerPassed = this.hasCasePassed(event);
			this.setCasePassed(event, false);
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
		this.setCasePassed(event, outerPassed);
		super.exit(event);
	}

	private Iterator<?> makeIterator(Event event) {
		return Arrays.stream(expression.getArray(event)).iterator();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "switch " + expression.toString(event, debug);
	}

	protected Mode switchMode() {
		return mode;
	}

	/**
	 * Marks the currently-evaluating switch case as having passed for fall-through mode.
	 */
	protected void setCasePassed(Event event, boolean passed) {
		this.hasPassed.set(event, passed);
	}

	/**
	 * Whether the currently-evaluating switch case has passed.
	 */
	protected boolean hasCasePassed(Event event) {
		return this.hasPassed.get(event);
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

	/**
	 * The mode this switch statement runs in.
	 * NORMAL = any matching cases run (similar to if if if)
	 * STRICT = the first matching case runs (similar to if else if else if)
	 * FALL_THROUGH = the first matching case runs, ALL following cases run (like java)
	 */
	public enum Mode {
		NORMAL,
		FALL_THROUGH,
		STRICT
	}

}
