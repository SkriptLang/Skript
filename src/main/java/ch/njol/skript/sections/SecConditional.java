package ch.njol.skript.sections;

import ch.njol.skript.ScriptLoader;
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
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.condition.Conditional;
import org.skriptlang.skript.lang.condition.Conditional.Operator;

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
	private @UnknownNullability Conditional<Event> conditional;
	private boolean ifAny;
	private boolean parseIf;
	private boolean parseIfPassed;
	private boolean multiline;

	private Kleenean hasDelayAfter;
	private @Nullable ExecutionIntent executionIntent;

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
		multiline = parseResult.regexes.isEmpty() && type != ConditionalType.ELSE;
		ParserInstance parser = getParser();

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
				Node nextNode = getNextNode(sectionNode, parser);
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
			Class<? extends Event>[] currentEvents = parser.getCurrentEvents();
			String currentEventName = parser.getCurrentEventName();

			List<Conditional<Event>> conditionals = new ArrayList<>();

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
						conditionals.add(condition);
					}
				}
				parser.setNode(sectionNode);
			} else {
				// otherwise, this is just a simple single line "if", with the condition on the same line
				String expr = parseResult.regexes.get(0).group();
				// Don't print a default error if 'if' keyword wasn't provided
				Condition condition = Condition.parse(expr, parseResult.hasTag("implicit") ? null : "Can't understand this condition: '" + expr + "'");
				if (condition != null)
					conditionals.add(condition);
			}

			if (parseIf) {
				parser.setCurrentEvents(currentEvents);
				parser.setCurrentEventName(currentEventName);
			}

			if (conditionals.isEmpty())
				return false;

			conditional = Conditional.compound(ifAny ? Operator.OR : Operator.AND, conditionals);
		}

		// ([else] parse if) If condition is valid and false, do not parse the section
		if (parseIf) {
			if (!checkConditions(ContextlessEvent.get())) {
				return true;
			}
			parseIfPassed = true;
		}

		Kleenean hadDelayBefore = parser.getHasDelayBefore();
		if (!multiline || type == ConditionalType.THEN)
			loadCode(sectionNode);

		// Get the execution intent of the entire conditional chain.
		if (type == ConditionalType.ELSE) {
			List<SecConditional> conditionals = getPrecedingConditionals(triggerItems);
			conditionals.add(0, this);
			for (SecConditional conditional : conditionals) {
				// Continue if the current conditional doesn't have executable code (the 'if' section of a multiline).
				if (conditional.multiline && conditional.type != ConditionalType.THEN)
					continue;

				// If the current conditional doesn't have an execution intent,
				//  then there is a possibility of the chain not stopping the execution.
				// Therefore, we can't assume anything about the intention of the chain,
				//  so we just set it to null and break out of the loop.
				ExecutionIntent triggerIntent = conditional.triggerExecutionIntent();
				if (triggerIntent == null) {
					executionIntent = null;
					break;
				}

				// If the current trigger's execution intent has a lower value than the chain's execution intent,
				//  then set the chain's intent to the trigger's
				if (executionIntent == null || triggerIntent.compareTo(executionIntent) < 0)
					executionIntent = triggerIntent;
			}
		}

		hasDelayAfter = parser.getHasDelayBefore();

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
				parser.setHasDelayBefore(Kleenean.TRUE);
			} else {
				// ... otherwise mark delayed as UNKNOWN.
				parser.setHasDelayBefore(Kleenean.UNKNOWN);
			}
		} else {
			if (!hasDelayAfter.isFalse()) {
				// If an if section or else-if section has some delay (definite or possible) in it,
				//  set the delayed Kleenean to UNKNOWN.
				parser.setHasDelayBefore(Kleenean.UNKNOWN);
			}
		}

		return true;
	}

	@Override
	public @Nullable TriggerItem getNext() {
		return getSkippedNext();
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		if (type == ConditionalType.THEN || (parseIf && !parseIfPassed)) {
			return getActualNext();
		} else if (parseIf || checkConditions(event)) {
			// if this is a multiline if, we need to run the "then" section instead
			SecConditional sectionToRun = multiline ? (SecConditional) getActualNext() : this;
			TriggerItem skippedNext = getSkippedNext();
			if (sectionToRun.last != null)
				sectionToRun.last.setNext(skippedNext);
			return sectionToRun.first != null ? sectionToRun.first : skippedNext;
		} else {
			return getActualNext();
		}
	}

	@Override
	public @Nullable ExecutionIntent executionIntent() {
		return executionIntent;
	}

	@Override
	public ExecutionIntent triggerExecutionIntent() {
		if (multiline && type != ConditionalType.THEN)
			// Handled in the 'then' section
			return null;
		return super.triggerExecutionIntent();
	}

	@Nullable
	private TriggerItem getSkippedNext() {
		TriggerItem next = getActualNext();
		while (next instanceof SecConditional nextSecCond && nextSecCond.type != ConditionalType.IF)
			next = next.getActualNext();
		return next;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String parseIf = this.parseIf ? "parse " : "";
		return switch (type) {
			case IF -> {
				if (multiline)
					yield parseIf + "if " + (ifAny ? "any" : "all");
				yield parseIf + "if " + conditional.toString(event, debug);
			}
			case ELSE_IF -> {
				if (multiline)
					yield "else " + parseIf + "if " + (ifAny ? "any" : "all");
				yield "else " + parseIf + "if " + conditional.toString(event, debug);
			}
			case ELSE -> "else";
			case THEN -> "then";
		};
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
	private static @Nullable SecConditional getPrecedingConditional(List<TriggerItem> triggerItems, @Nullable ConditionalType type) {
		// loop through the triggerItems in reverse order so that we find the most recent items first
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (triggerItem instanceof SecConditional precedingSecConditional) {
				if (precedingSecConditional.type == ConditionalType.ELSE) {
					// if the conditional is an else, return null because it belongs to a different condition and ends
					// this one
					return null;
				} else if (type == null || precedingSecConditional.type == type) {
					// if the conditional matches the type argument, we found our most recent preceding conditional section
					return precedingSecConditional;
				}
			} else {
				return null;
			}
		}
		return null;
	}

	private static List<SecConditional> getPrecedingConditionals(List<TriggerItem> triggerItems) {
		List<SecConditional> conditionals = new ArrayList<>();
		// loop through the triggerItems in reverse order so that we find the most recent items first
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (!(triggerItem instanceof SecConditional conditional))
				break;
			if (conditional.type == ConditionalType.ELSE)
				// if the conditional is an else, break because it belongs to a different condition and ends
				// this one
				break;
			conditionals.add(conditional);
		}
		return conditionals;
	}

	private static List<SecConditional> getElseIfs(List<TriggerItem> triggerItems) {
		List<SecConditional> list = new ArrayList<>();
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (triggerItem instanceof SecConditional precedingSecConditional && precedingSecConditional.type == ConditionalType.ELSE_IF) {
				list.add(precedingSecConditional);
			} else {
				break;
			}
		}
		return list;
	}

	private boolean checkConditions(Event event) {
		return conditional == null || conditional.evaluate(event).isTrue();
	}

	private @Nullable Node getNextNode(Node precedingNode, ParserInstance parser) {
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
