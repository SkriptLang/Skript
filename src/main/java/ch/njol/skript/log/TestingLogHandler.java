package ch.njol.skript.log;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.test.runner.EvtTestCase;
import ch.njol.skript.test.runner.TestTracker;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.logging.Level;

/**
 * Counts logged messages of a certain type and logs parse errors to {@link TestTracker}
 */
public class TestingLogHandler extends LogHandler {

	private final int minimum;
	private int count;
	private final ParserInstance parser;

	public TestingLogHandler(Level minimum) {
		this.minimum = minimum.intValue();
		this.parser = ParserInstance.get();
	}

	@Override
	public LogResult log(LogEntry entry) {
		if (entry.level.intValue() >= minimum) {
			count++;
			Structure struct = parser.getCurrentStructure();
			Node node = parser.getNode();

			String name = struct instanceof EvtTestCase test ? test.getTestName() : struct != null ? struct.getSyntaxTypeName() : null;
			TestTracker.parsingStarted(name);

			if (node != null) {
				TestTracker.testFailed(entry.getMessage(), parser.getCurrentScript(), node.getLine());
			} else {
				TestTracker.testFailed(entry.getMessage());
			}
		}
		return LogResult.LOG;
	}

	@Override
	public TestingLogHandler start() {
		SkriptLogger.startLogHandler(this);
		return this;
	}

	public int getCount() {
		return count;
	}

}
