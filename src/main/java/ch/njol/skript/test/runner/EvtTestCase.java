package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.*;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class EvtTestCase extends SkriptEvent {

	static {
		if (TestMode.ENABLED) {
			Skript.registerEvent("Test Case", EvtTestCase.class, SkriptTestEvent.class, "test %string% [when <.+>]")
					.description("Contents represent one test case.")
					.examples("")
					.since("2.5");

			EventValues.registerEventValue(SkriptTestEvent.class, Block.class, new Getter<>() {
				@Override
				public @NotNull Block get(SkriptTestEvent ignored) {
					return SkriptJUnitTest.getBlock();
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(SkriptTestEvent.class, Location.class, new Getter<>() {
				@Override
				public @NotNull Location get(SkriptTestEvent ignored) {
					return SkriptJUnitTest.getTestLocation();
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(SkriptTestEvent.class, World.class, new Getter<>() {
				@Override
				public @NotNull World get(SkriptTestEvent ignored) {
					return SkriptJUnitTest.getTestWorld();
				}
			}, EventValues.TIME_NOW);
		}
	}

	private Expression<String> name;

	@Nullable
	private Condition condition;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		name = (Expression<String>) args[0];
		if (!parseResult.regexes.isEmpty()) { // Do not parse or run unless condition is met
			String cond = parseResult.regexes.get(0).group();
			condition = Condition.parse(cond, "Can't understand this condition: " + cond);
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		String n = name.getSingle(event);
		if (n == null)
			return false;
		Skript.info("Running test case " + n);
		TestTracker.testStarted(n);
		return true;
	}

	@Override
	public boolean shouldLoadEvent() {
		return condition != null ? condition.check(new SkriptTestEvent()) : true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (event != null)
			return "test " + name.getSingle(event);
		return "test case";
	}

}
