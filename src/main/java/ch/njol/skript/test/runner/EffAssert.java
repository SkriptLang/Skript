package ch.njol.skript.test.runner;

import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.VerboseAssert;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.script.Script;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;

@Name("Assert")
@Description("Assert that condition is true. Test fails when it is not.")
@NoDoc
public class EffAssert extends Effect  {

	static {
		if (TestMode.ENABLED)
			Skript.registerEffect(EffAssert.class,
					"assert <.+> [(1:to fail)] with [error] %string%",
					"assert <.+> [(1:to fail)] with [error] %string%, expected [value] %object%, [and] (received|got) [value] %object%");
	}

	@Nullable
	private Condition condition;
	private Script script;
	private int line;

	private Expression<String> errorMsg;
	@Nullable
	private Expression<?> expected;
	@Nullable
	private Expression<?> got;
	private boolean shouldFail;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (isDelayed == Kleenean.TRUE && !TestMode.JUNIT && !TestMode.DEV_MODE) {
			Skript.error("Assertions cannot be delayed");
			return false;
		}

		String conditionString = parseResult.regexes.get(0).group();
		errorMsg = (Expression<String>) exprs[0];
		boolean canInit = true;
		if (exprs.length > 1) {
			expected = LiteralUtils.defendExpression(exprs[1]);
			got = LiteralUtils.defendExpression(exprs[2]);
			canInit = LiteralUtils.canInitSafely(expected, got);
		}
		shouldFail = parseResult.mark != 0;
		script = getParser().getCurrentScript();
		Node node = getParser().getNode();
		line = node != null ? node.getLine() : -1;
		
		ParseLogHandler logHandler = SkriptLogger.startParseLogHandler();
		try {
			condition = Condition.parse(conditionString, "Can't understand this condition: " + conditionString);

			if (shouldFail)
				return true;
			
			if (condition == null) {
				logHandler.printError();
			} else {
				logHandler.printLog();
			}
		} finally {
			logHandler.stop();
		}

		return (condition != null) && canInit;
	}

	@Override
	protected void execute(Event event) {}

	@Nullable
	@Override
	public TriggerItem walk(Event event) {
		if (shouldFail && condition == null)
			return getNext();

		if (condition.check(event) == shouldFail) {
			String message = errorMsg.getSingle(event);
			assert message != null; // Should not happen, developer needs to fix test.

			// generate expected/got message if possible
			String expectedMessage = "";
			String gotMessage = "";
			if (expected != null)
				expectedMessage = VerboseAssert.getExpressionValue(expected, event);
			if (got != null)
				gotMessage = VerboseAssert.getExpressionValue(got, event);

			if (condition instanceof VerboseAssert) {
				if (expectedMessage.isEmpty())
					expectedMessage = ((VerboseAssert) condition).getExpectedMessage(event);
				if (gotMessage.isEmpty())
					gotMessage = ((VerboseAssert) condition).getReceivedMessage(event);
			}

			if (!expectedMessage.isEmpty() && !gotMessage.isEmpty())
				message += " (Expected " + expectedMessage + ", but got " + gotMessage + ")";

			if (SkriptJUnitTest.getCurrentJUnitTest() != null) {
				TestTracker.junitTestFailed(SkriptJUnitTest.getCurrentJUnitTest(), message);
			} else {
				if (line >= 0) {
					TestTracker.testFailed(message, script, line);
				} else {
					TestTracker.testFailed(message, script);
				}
			}
			return null;
		}
		return getNext();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (condition == null)
			return "assertion";
		return "assert " + condition.toString(event, debug);
	}

}
