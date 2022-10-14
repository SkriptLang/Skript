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
import ch.njol.skript.events.bukkit.SkriptParseEvent;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NotNullFieldNotInitialized")
public class SecConditional extends Section {

	private final static Patterns<ConditionalType> CONDITIONAL_PATTERNS = new Patterns<>(new Object[][] {
		{"else", ConditionalType.ELSE},
		{"else [(:parse)] if [<.+>]", ConditionalType.ELSE_IF},
		{"[(:parse)] if (any:any|any:at least one [of])", ConditionalType.IF},
		{"[(:parse)] if [all]", ConditionalType.IF},
		{"[(:parse)] if <.+>", ConditionalType.IF},
		{"then [run|perform|do]", ConditionalType.THEN},
		{"(inline:<.+>)", ConditionalType.IF}
	});

	static {
		Skript.registerSection(SecConditional.class, CONDITIONAL_PATTERNS.getPatterns());
	}

	private enum ConditionalType {
		ELSE, ELSE_IF, IF, THEN
	}

	private ConditionalType type;
	private List<Condition> conditions = new ArrayList<>();
	private boolean ifAny;
	private boolean parseIf;
	private boolean parseIfPassed;
	private boolean inline;
	private boolean multiline;

	private Kleenean hasDelayAfter;

	@Override
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						SectionNode sectionNode,
						List<TriggerItem> triggerItems) {
		type = CONDITIONAL_PATTERNS.getInfo(matchedPattern);
		ifAny = parseResult.tags.contains("any");
		parseIf = parseResult.tags.contains("parse");
		inline = parseResult.tags.contains("inline");
		multiline = parseResult.regexes.size() == 0 && type != ConditionalType.ELSE;
		// if this an an "if" or "else if", let's try to parse the conditions right away
		if (type == ConditionalType.IF || type == ConditionalType.ELSE_IF) {

			ParserInstance parser = getParser();
			Class<? extends Event>[] currentEvents = parser.getCurrentEvents();
			String currentEventName = parser.getCurrentEventName();
			Structure currentStructure = parser.getCurrentStructure();

			// Change event if using 'parse if'
			if (parseIf) {
				//noinspection unchecked
				parser.setCurrentEvents(new Class[]{SkriptParseEvent.class});
				parser.setCurrentEventName("parse");
				parser.setCurrentStructure(null);
			}

			// if this is a multiline "if", we have to parse each line
			if (multiline) {
				if (sectionNode.isEmpty()) {
					Skript.error("'if' sections must contain at least one condition");
					return false;
				}
				for (Node n : sectionNode) {
					String key = n.getKey();
					if (key != null) {
						SkriptLogger.setNode(n);
						Condition condition = Condition.parse(key, "Can't understand this condition: '" + key + "'");
						if (condition != null)
							conditions.add(condition);
					}
				}
				SkriptLogger.setNode(sectionNode);
			} else {
				// otherwise, this is just a simple single line "if", with the condition in the syntax
				String expr = parseResult.regexes.get(0).group();
				// Don't print a default error if 'if' keyword wasn't provided
				Condition condition = Condition.parse(expr, inline ? null : "Can't understand this condition: '" + expr + "'");
				if (condition != null)
					conditions.add(condition);
			}

			if (parseIf) {
				parser.setCurrentEvents(currentEvents);
				parser.setCurrentEventName(currentEventName);
				parser.setCurrentStructure(currentStructure);
			}

			if (conditions.isEmpty())
				return false;
		}

		SecConditional lastIf;
		if (type != ConditionalType.IF) {
			lastIf = getClosestIf(triggerItems);
			if (lastIf == null) {
				if (type == ConditionalType.ELSE_IF)
					Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
				else
					Skript.error("'else' has to be placed just after another 'if' or 'else if' section");
				return false;
			}
		} else {
			lastIf = null;
		}

		// ([else] parse if) If condition is valid and false, do not parse the section
		if (parseIf) {
			if (!checkConditions(new SkriptParseEvent())) {
				return true;
			}
			parseIfPassed = true;
		}

		Kleenean hadDelayBefore = getParser().getHasDelayBefore();

		if (!multiline || type == ConditionalType.ELSE || type == ConditionalType.THEN) {
			loadCode(sectionNode);
		}
		hasDelayAfter = getParser().getHasDelayBefore();

		// If the code definitely has a delay before this section, or if the section did not alter the delayed Kleenean,
		//  there's no need to change the Kleenean.
		if (hadDelayBefore.isTrue() || hadDelayBefore.equals(hasDelayAfter))
			return true;

		if (type == ConditionalType.ELSE) {
			// In an else section, ...
			if (hasDelayAfter.isTrue()
					&& lastIf.hasDelayAfter.isTrue()
					&& getElseIfs(triggerItems).stream().map(SecConditional::getHasDelayAfter).allMatch(Kleenean::isTrue)) {
				// ... if the if section, all else-if sections and the else section have definite delays,
				//  mark delayed as TRUE.
				getParser().setHasDelayBefore(Kleenean.TRUE);
			} else {
				// ... otherwise mark delayed as UNKNOWN.
				getParser().setHasDelayBefore(Kleenean.UNKNOWN);
			}
		} else {
			if (!hasDelayAfter.isFalse()) {
				// If an if section or else-if section has some delay (definite or possible) in it,
				//  set the delayed Kleenean to UNKNOWN.
				getParser().setHasDelayBefore(Kleenean.UNKNOWN);
			}
		}

		return true;
	}

	@Override
	@Nullable
	public TriggerItem getNext() {
		return getSkippedNext();
	}

	@Nullable
	public TriggerItem getNormalNext() {
		return super.getNext();
	}

	@Nullable
	@Override
	protected TriggerItem walk(Event event) {
		if (type == ConditionalType.THEN || (parseIf && !parseIfPassed)) {
			return getNormalNext();
		} else if (type == ConditionalType.ELSE || parseIf || checkConditions(event)) {
			TriggerItem skippedNext = getSkippedNext();
			if (multiline) {
				SecConditional thenSection = (SecConditional) getNormalNext();
				assert thenSection != null;
				if (thenSection.last != null)
					thenSection.last.setNext(skippedNext);
				return thenSection.first != null ? thenSection.first : skippedNext;
			} else {
				if (last != null)
					last.setNext(skippedNext);
				return first != null ? first : skippedNext;
			}
		} else {
			return getNormalNext();
		}
	}

	@Nullable
	private TriggerItem getSkippedNext() {
		TriggerItem next = getNormalNext();
		while (next instanceof SecConditional && ((SecConditional) next).type != ConditionalType.IF)
			next = ((SecConditional) next).getNormalNext();
		return next;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		String parseIf = this.parseIf ? "parse " : "";
		switch (type) {
			case IF:
				if (multiline)
					return parseIf + "if (multiline)";
				return parseIf + "if " + conditions.get(0).toString(e, debug);
			case ELSE_IF:
				if (multiline)
					return "else " + parseIf + "if (multiline)";
				return "else " + parseIf + "if " + conditions.get(0).toString(e, debug);
			case ELSE:
				return "else";
			case THEN:
				return "then";
			default:
				throw new IllegalStateException();
		}
	}

	private Kleenean getHasDelayAfter() {
		return hasDelayAfter;
	}

	@Nullable
	private static SecConditional getClosestIf(List<TriggerItem> triggerItems) {
		// loop through the triggerItems in reverse order so that we find the most recent items first
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (triggerItem instanceof SecConditional) {
				SecConditional secConditional = (SecConditional) triggerItem;

				if (secConditional.type == ConditionalType.IF)
					// if the condition is an if, we found our most recent preceding "if"
					return secConditional;
				else if (secConditional.type == ConditionalType.ELSE)
					// if the conditional is an else, return null because it belongs to a different condition and ends
					// this one
					return null;
			} else {
				return null;
			}
		}
		return null;
	}

	private static List<SecConditional> getElseIfs(List<TriggerItem> triggerItems) {
		List<SecConditional> list = new ArrayList<>();
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (triggerItem instanceof SecConditional) {
				SecConditional secConditional = (SecConditional) triggerItem;

				if (secConditional.type == ConditionalType.ELSE_IF)
					list.add(secConditional);
				else
					break;
			} else {
				break;
			}
		}
		return list;
	}

	private boolean checkConditions(Event event) {
		if (this.type == ConditionalType.IF || this.type == ConditionalType.ELSE_IF) {
			if (this.ifAny) {
				return conditions.stream().anyMatch(c -> c.check(event));
			}
			return conditions.stream().allMatch(c -> c.check(event));
		}
		return true;
	}

}
