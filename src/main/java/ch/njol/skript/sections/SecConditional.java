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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Name("Conditionals")
@Description({
	"Conditional sections",
	"if: executed when its condition is true",
	"else if: executed if all previous chained conditionals weren't executed, and its condition is true",
	"else: executed if all previous chained conditionals weren't executed",
	"",
	"parse if: a special case of 'if' condition that its code will not be parsed if the condition is not true",
	"else parse if: another special case of 'else if' condition that its code will not be parsed if all previous chained " +
		"conditionals weren't executed, and its condition is true",
})
@Examples({
	"if player's health is greater than or equal to 4:",
	"\tsend \"Your health is okay so far but be careful!\"",
	"",
	"else if player's health is greater than 2:",
	"\tsend \"You need to heal ASAP, your health is very low!\"",
	"",
	"else: # Less than 2 hearts",
	"\tsend \"You are about to DIE if you don't heal NOW. You have only %player's health% heart(s)!\"",
	"",
	"parse if plugin \"SomePluginName\" is enabled: # parse if %condition%",
	"\t# This code will only be executed if the condition used is met otherwise Skript will not parse this section therefore will not give any errors/info about this section",
	""
})
@Since("1.0")
@SuppressWarnings("NotNullFieldNotInitialized")
public class SecConditional extends Section {

	private static final SkriptPattern THEN_PATTERN = PatternCompiler.compile("then [run]");
	private static final Patterns<ConditionalType> CONDITIONAL_PATTERNS = new Patterns<>(new Object[][] {
		{"else", ConditionalType.ELSE},
		{"else [:parse] if <.+>", ConditionalType.ELSE_IF},
		{"else [:parse] if (:any|any:at least one [of])", ConditionalType.ELSE_IF},
		{"else [:parse] if [all]", ConditionalType.ELSE_IF},
		{"[:parse] if (:any|any:at least one [of])", ConditionalType.IF},
		{"[:parse] if [all]", ConditionalType.IF},
		{"[:parse] if <.+>", ConditionalType.IF},
		{THEN_PATTERN.toString(), ConditionalType.THEN},
		{"implicit:<.+>", ConditionalType.IF}
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
		ifAny = parseResult.hasTag("any");
		parseIf = parseResult.hasTag("parse");
		multiline = parseResult.regexes.size() == 0 && type != ConditionalType.ELSE;

		// ensure this conditional is chained correctly (e.g. an else must have an if)
		if (type != ConditionalType.IF) {
			if (type == ConditionalType.THEN) {
				/*
				 * if this is a 'then' section, the preceding conditional has to be a multiline conditional section
				 * otherwise, you could put a 'then' section after a non-multiline 'if'. for example:
				 *  if 1 is 1:
				 *    set {_example} to true
				 *  then: # this shouldn't be possible
				 *    set {_uh oh} to true
				 */
				SecConditional precedingConditional = getPrecedingConditional(triggerItems, null);
				if (precedingConditional == null || !precedingConditional.multiline) {
					Skript.error("'then' has to placed just after a multiline 'if' or 'else if' section");
					return false;
				}
			} else {
				// find the latest 'if' section so that we can ensure this section is placed properly (e.g. ensure a 'if' occurs before an 'else')
				SecConditional precedingIf = getPrecedingConditional(triggerItems, ConditionalType.IF);
				if (precedingIf == null) {
					if (type == ConditionalType.ELSE_IF) {
						Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
					} else if (type == ConditionalType.ELSE) {
						Skript.error("'else' has to be placed just after another 'if' or 'else if' section");
					} else if (type == ConditionalType.THEN) {
						Skript.error("'then' has to placed just after a multiline 'if' or 'else if' section");
					}
					return false;
				}
			}
		} else {
			// if this is a multiline if, we need to check if there is a "then" section after this
			if (multiline) {
				Node nextNode = getNextNode(sectionNode, getParser());
				String error = (ifAny ? "'if any'" : "'if all'") + " has to be placed just before a 'then' section";
				if (nextNode instanceof SectionNode && nextNode.getKey() != null) {
					String nextNodeKey = ScriptLoader.replaceOptions(nextNode.getKey());
					if (THEN_PATTERN.match(nextNodeKey) == null) {
						Skript.error(error);
						return false;
					}
				} else {
					Skript.error(error);
					return false;
				}
			}
		}

		// if this an "if" or "else if", let's try to parse the conditions right away
		if (type == ConditionalType.IF || type == ConditionalType.ELSE_IF) {
			ParserInstance parser = getParser();
			Class<? extends Event>[] currentEvents = parser.getCurrentEvents();
			String currentEventName = parser.getCurrentEventName();

			// Change event if using 'parse if'
			if (parseIf) {
				//noinspection unchecked
				parser.setCurrentEvents(new Class[]{ContextlessEvent.class});
				parser.setCurrentEventName("parse");
			}

			// if this is a multiline "if", we have to parse each line as its own condition
			if (multiline) {
				// we have to get the size of the iterator here as SectionNode#size includes empty/void nodes
				int nonEmptyNodeCount = Iterables.size(sectionNode);
				if (nonEmptyNodeCount < 2) {
					Skript.error((ifAny ? "'if any'" : "'if all'") + " sections must contain at least two conditions");
					return false;
				}
				for (Node childNode : sectionNode) {
					if (childNode instanceof SectionNode) {
						Skript.error((ifAny ? "'if any'" : "'if all'") + " sections may not contain other sections");
						return false;
					}
					String childKey = childNode.getKey();
					if (childKey != null) {
						childKey = ScriptLoader.replaceOptions(childKey);
						parser.setNode(childNode);
						Condition condition = Condition.parse(childKey, "Can't understand this condition: '" + childKey + "'");
						// if this condition was invalid, don't bother parsing the rest
						if (condition == null)
							return false;
						conditions.add(condition);
					}
				}
				parser.setNode(sectionNode);
			} else {
				// otherwise, this is just a simple single line "if", with the condition on the same line
				String expr = parseResult.regexes.get(0).group();
				// Don't print a default error if 'if' keyword wasn't provided
				Condition condition = Condition.parse(expr, parseResult.hasTag("implicit") ? null : "Can't understand this condition: '" + expr + "'");
				if (condition != null)
					conditions.add(condition);
			}

			if (parseIf) {
				parser.setCurrentEvents(currentEvents);
				parser.setCurrentEventName(currentEventName);
			}

			if (conditions.isEmpty())
				return false;
		}

		// ([else] parse if) If condition is valid and false, do not parse the section
		if (parseIf) {
			if (!checkConditions(ContextlessEvent.get())) {
				return true;
			}
			parseIfPassed = true;
		}

		Kleenean hadDelayBefore = getParser().getHasDelayBefore();
		if (!multiline || type == ConditionalType.THEN)
			loadCode(sectionNode);
		hasDelayAfter = getParser().getHasDelayBefore();

		// If the code definitely has a delay before this section, or if the section did not alter the delayed Kleenean,
		//  there's no need to change the Kleenean.
		if (hadDelayBefore.isTrue() || hadDelayBefore.equals(hasDelayAfter))
			return true;

		if (type == ConditionalType.ELSE) {
			SecConditional precedingIf = getPrecedingConditional(triggerItems, ConditionalType.IF);
			assert precedingIf != null; // at this point, we've validated the section so this can't be null
			// In an else section, ...
			if (hasDelayAfter.isTrue()
					&& precedingIf.hasDelayAfter.isTrue()
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
		} else if (parseIf || checkConditions(event)) {
			// if this is a multiline if, we need to run the "then" section instead
			SecConditional sectionToRun = multiline ? (SecConditional) getNormalNext() : this;
			TriggerItem skippedNext = getSkippedNext();
			if (sectionToRun.last != null)
				sectionToRun.last.setNext(skippedNext);
			return sectionToRun.first != null ? sectionToRun.first : skippedNext;
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
	public String toString(@Nullable Event event, boolean debug) {
		String parseIf = this.parseIf ? "parse " : "";
		switch (type) {
			case IF:
				if (multiline)
					return parseIf + "if " + (ifAny ? "any" : "all");
				return parseIf + "if " + conditions.get(0).toString(event, debug);
			case ELSE_IF:
				if (multiline)
					return "else " + parseIf + "if " + (ifAny ? "any" : "all");
				return "else " + parseIf + "if " + conditions.get(0).toString(event, debug);
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

	/**
	 * Gets the closest conditional section in the list of trigger items
	 * @param triggerItems the list of items to search for the closest conditional section in
	 * @param type the type of conditional section to find. if null is provided, any type is allowed.
	 * @return the closest conditional section
	 */
	@Nullable
	private static SecConditional getPrecedingConditional(List<TriggerItem> triggerItems, @Nullable ConditionalType type) {
		// loop through the triggerItems in reverse order so that we find the most recent items first
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (triggerItem instanceof SecConditional) {
				SecConditional conditionalSection = (SecConditional) triggerItem;

				if (conditionalSection.type == ConditionalType.ELSE) {
					// if the conditional is an else, return null because it belongs to a different condition and ends
					// this one
					return null;
				} else if (type == null || conditionalSection.type == type) {
					// if the conditional matches the type argument, we found our most recent preceding conditional section
					return conditionalSection;
				}
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
		if (conditions.isEmpty()) { // else and then
			return true;
		} else if (ifAny) {
			return conditions.stream().anyMatch(c -> c.check(event));
		} else {
			return conditions.stream().allMatch(c -> c.check(event));
		}
	}

	@Nullable
	private Node getNextNode(Node precedingNode, ParserInstance parser) {
		// iterating over the parent node causes the current node to change, so we need to store it to reset it later
		Node originalCurrentNode = parser.getNode();
		SectionNode parentNode = precedingNode.getParent();
		if (parentNode == null)
			return null;
		Iterator<Node> parentIterator = parentNode.iterator();
		while (parentIterator.hasNext()) {
			Node current = parentIterator.next();
			if (current == precedingNode) {
				Node nextNode = parentIterator.hasNext() ? parentIterator.next() : null;
				parser.setNode(originalCurrentNode);
				return nextNode;
			}
		}
		parser.setNode(originalCurrentNode);
		return null;
	}

}
