package ch.njol.skript.sections;

import ch.njol.skript.Skript;
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
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.condition.Conditional;
import org.skriptlang.skript.lang.condition.Conditional.Operator;

import java.util.ArrayList;
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
	"",
	"while all:",
		"\tplayer is online",
		"\tplayer's experience < 200",
	"do:",
		"\twait 1 second",
		"\tadd 2 to player's experience",
	"",
	"while any:",
		"\tplayer's tool is {_item}",
		"\tplayer's offhand tool is {_item}",
	"do:",
		"\theal player by 1"
})
@Since("2.0, 2.6 (do while), INSERT VERSION (all, any)")
public class SecWhile extends LoopSection {

	private static final SkriptPattern DO_PATTERN = PatternCompiler.compile("do");

	private enum WhileState {
		NORMAL, ANY, DO
	}

	private static final Patterns<WhileState> WHILE_STATE_PATTERNS = new Patterns<>(new Object[][] {
		{"[:do] while <.+>", WhileState.NORMAL},
		{"[:do] while [all]", WhileState.NORMAL},
		{"[:do] while (any|at least one of)", WhileState.ANY},
		{DO_PATTERN.toString(), WhileState.DO}
	});

	static {
		Skript.registerSection(SecWhile.class, WHILE_STATE_PATTERNS.getPatterns());
	}

	private @Nullable TriggerItem actualNext;
	private boolean doWhile;
	private boolean ranDoWhile = false;
	private WhileState state;
	private boolean multiline;
	private @UnknownNullability Conditional<Event> conditional;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		state = WHILE_STATE_PATTERNS.getInfo(matchedPattern);
		doWhile = parseResult.hasTag("do");
		multiline = matchedPattern == 1 || matchedPattern == 2;
		ParserInstance parser = getParser();

		String prefixError = "while " + (state == WhileState.ANY ? "any" : "all");
		if (state == WhileState.DO) {
			// This instance is the 'do' , so now we have to check to make sure it is following directly after a 'while all' or 'while any'
			SecWhile precedingSecWhile = getPrecedingElement(SecWhile.class, triggerItems, secWhile -> secWhile.multiline);
			if (precedingSecWhile == null) {
				Skript.error("'do' has to be placed just after a multiline 'while all' or 'while any' section");
				return false;
			}
			// Grab the conditions that were defined in the preceding SecWhile to be used in this instance
			conditional = precedingSecWhile.conditional;
			// Check to see if the preceding SecWhile used "[:do]" to be used in this instance
			doWhile = precedingSecWhile.doWhile;
		} else if (multiline) {
			// This instance is a 'while all' or 'while any'
			// So we have to check to make sure a 'do' section is followed directly after
			boolean checkSubsiding = checkFollowingElement(sectionNode, parser, DO_PATTERN);
			if (!checkSubsiding) {
				Skript.error(prefixError + " has to be placed just before a 'do' section.");
				return false;
			}
		}

		if (state != WhileState.DO) {
			List<Conditional<Event>> conditionals = new ArrayList<>();

			if (multiline) {
				conditionals = parseMultiline(sectionNode, parser, prefixError);
				if (conditionals == null)
					return false;
			} else {
				String expr = parseResult.regexes.get(0).group();
				Condition condition = Condition.parse(expr, "Can't understand this condition: '" + expr +  "'");
				if (condition == null)
					return false;
				conditionals.add(condition);
			}

			conditional = Conditional.compound(state == WhileState.ANY ? Operator.OR : Operator.AND, conditionals);
		}

		loadOptionalCode(sectionNode);
		super.setNext(this);

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		if (multiline) {
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
	protected @Nullable ExecutionIntent triggerExecutionIntent() {
		if (multiline && state != WhileState.DO)
			return null;
		return super.triggerExecutionIntent();
	}

	@Override
	public @Nullable ExecutionIntent executionIntent() {
		return doWhile ? triggerExecutionIntent() : null;
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
		if (doWhile && state != WhileState.DO)
			builder.append("do");
		builder.append(switch (state) {
			case NORMAL -> "while";
			case ANY -> "while any";
			case DO -> "do";
		});
		builder.append(conditional);
		return builder.toString();
	}

	@Override
	public void exit(Event event) {
		ranDoWhile = false;
		super.exit(event);
	}

}
