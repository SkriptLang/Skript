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
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
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

@Name("While Loop")
@Description("While Loop sections are loops that will just keep repeating as long as a condition is met.")
@Examples({
	"while size of all players < 5:",
	"\tsend \"More players are needed to begin the adventure\" to all players",
	"\twait 5 seconds",
	"",
	"set {_counter} to 1",
	"do while {_counter} > 1: # false but will increase {_counter} by 1 then get out",
	"\tadd 1 to {_counter}",
	"",
	"# Be careful when using while loops with conditions that are almost ",
	"# always true for a long time without using 'wait %timespan%' inside it, ",
	"# otherwise it will probably hang and crash your server.",
	"while player is online:",
	"\tgive player 1 dirt",
	"\twait 1 second # without using a delay effect the server will crash",
})
@Since("2.0, 2.6 (do while)")
public class SecWhile extends LoopSection {

	private static final SkriptPattern DO_PATTERN = PatternCompiler.compile("do");

	private enum WhileState{
		NORMAL("[:do] while <.+>"),
		ALL("[:do] while all"),
		ANY("[:do] while (any|at least one of)"),
		DO("do");

		private String pattern;

		WhileState(String pattern) {
			this.pattern = pattern;
		}
	}

	private static final WhileState[] WHILE_STATES = WhileState.values();

	static {
		String[] patterns  = new String[WHILE_STATES.length];
		for (WhileState state : WHILE_STATES) {
			patterns[state.ordinal()] = state.pattern;
		}
		Skript.registerSection(SecWhile.class, patterns);
	}

	private @Nullable TriggerItem actualNext;
	private boolean doWhile;
	private boolean ranDoWhile = false;
	private WhileState selectedState;
	private boolean multiline;
	private @UnknownNullability Conditional<Event> conditional;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		selectedState = WHILE_STATES[matchedPattern];
		doWhile = parseResult.hasTag("do");
		multiline = parseResult.regexes.isEmpty() && selectedState != WhileState.DO && selectedState != WhileState.NORMAL;
		ParserInstance parser = getParser();

		String prefixError = "while " + (selectedState == WhileState.ALL ? "all" : "any");
		if (selectedState == WhileState.DO) {
			// This instance is the 'do' ,  so now we have to check to make sure it is following directly after a 'while all' or 'while any'
			SecWhile precedingSecWhile = getPrecedingWhile(triggerItems, null);
			if (precedingSecWhile == null || !precedingSecWhile.multiline) {
				Skript.error("'do' has to be placed just after a multiline 'while all' or 'while any' section");
				return false;
			}
			// Grab the conditions that were defined in the 'while all' or 'while any' to be used in this instance
			conditional = precedingSecWhile.getConditional();
		} else if (multiline) {
			// This instance is a 'while all' or 'while any'
			// So we have to check to make sure a 'do' section is followed directly after
			Node nextNode = getNextNode(sectionNode, parser);
			String error = prefixError + " has to be placed just before a 'do' section.";
			if (nextNode instanceof SectionNode && nextNode.getKey() != null) {
				String nextKey = ScriptLoader.replaceOptions(nextNode.getKey());
				if (DO_PATTERN.match(nextKey) == null) {
					Skript.error(error);
					return false;
				}
			} else {
				Skript.error(error);
				return false;
			}
		}

		if (selectedState != WhileState.DO) {
			List<Conditional<Event>> conditionals = new ArrayList<>();

			if (multiline) {
				int nonEmptyNodeCount = Iterables.size(sectionNode);
				if (nonEmptyNodeCount < 2) {
					Skript.error(prefixError + " sections must contain at least two conditions.");
					return false;
				}
				for (Node childNode : sectionNode) {
					if (childNode instanceof SectionNode) {
						Skript.error(prefixError + " sections may not contain other sections.");
						return false;
					}
					String childKey = childNode.getKey();
					if (childKey == null)
						continue;
					childKey = ScriptLoader.replaceOptions(childKey);
					parser.setNode(childNode);
					Condition condition1 = Condition.parse(childKey, "Can't understand the condition: '" + childKey + "'");
					if (condition1 == null)
						return false;
					conditionals.add(condition1);
				}
				parser.setNode(sectionNode);
			} else {
				String expr = parseResult.regexes.get(0).group();
				Condition condition1 = Condition.parse(expr, parseResult.hasTag("implicit") ? null : "Can't understand this condition: '" + expr +  "'");
				if (condition1 == null)
					return false;
				conditionals.add(condition1);
			}

			conditional = Conditional.compound(selectedState == WhileState.ANY ? Operator.OR : Operator.AND, conditionals);
		}

		loadOptionalCode(sectionNode);
		super.setNext(this);

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		if (selectedState == WhileState.ALL || selectedState == WhileState.ANY) {
			// Condition checking is handled in the correlating 'do' section
			// So we can skip this
			return actualNext;
		}
		if ((doWhile && !ranDoWhile) || conditional.evaluate(event).isTrue()) {
			ranDoWhile = true;
			currentLoopCounter.put(event, (currentLoopCounter.getOrDefault(event, 0L)) + 1);
            return walk(event, true);
		}
		exit(event);
		debug(event, false);
		return actualNext;
	}

	@Override
	public @Nullable ExecutionIntent executionIntent() {
		if (multiline && (selectedState == WhileState.ALL || selectedState == WhileState.ANY))
			return null;
		return doWhile ? triggerExecutionIntent() : super.executionIntent();
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
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		switch (selectedState) {
			case NORMAL -> {
				if (doWhile)
					builder.append("do");
				builder.append("while");
			}
			case ALL -> builder.append("while all");
			case ANY -> builder.append("while any");
			case DO -> builder.append("do");
		}
		builder.append(conditional);
		return builder.toString();
	}

	@Override
	public void exit(Event event) {
		ranDoWhile = false;
		super.exit(event);
	}

	private Conditional<Event> getConditional() {
		return conditional;
	}

	private static @Nullable SecWhile getPrecedingWhile(List<TriggerItem> triggerItems, @Nullable WhileState state) {
		for (int i = triggerItems.size() - 1; i >= 0; i++) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (triggerItem instanceof SecWhile precedingSecWhile) {
				if (state == null || precedingSecWhile.selectedState == state)
					return precedingSecWhile;
			} else {
				return null;
			}
		}
		return null;
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
