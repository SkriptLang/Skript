package ch.njol.skript.lang;

import ch.njol.skript.config.Config;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.structures.StructVariables.DefaultVariables;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.variables.Variables;
import ch.njol.util.NonNullPair;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.lang.script.Script;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests which variable names record default variable type hints. Local variables never use default variables,
 * so they don't open a hint scope, and a hint from a local's name would land in an enclosing global's scope.
 */
public class DefaultVariableHintTest extends SkriptJUnitTest {

	private static class HintEvent extends Event {

		private static final HandlerList HANDLERS = new HandlerList();

		@Override
		public @NotNull HandlerList getHandlers() {
			return HANDLERS;
		}

		@SuppressWarnings("unused")
		public static HandlerList getHandlerList() {
			return HANDLERS;
		}

	}

	private final Event event = new HintEvent();
	private DefaultVariables data;

	@Before
	public void setUp() throws IOException {
		Config config = new Config(new ByteArrayInputStream(new byte[0]), "default variable hints.sk", false, false, ":");
		Script script = new Script(config, List.of());
		data = new DefaultVariables(List.of(new NonNullPair<>("hint::<string>", "default")));
		script.addData(data);
		ParserInstance.get().setActive(script);
		Variables.setVariable("k", "key", event, true);
	}

	@After
	public void cleanUp() {
		ParserInstance.get().setInactive();
		Variables.removeLocals(event);
	}

	@Test
	public void globalNameRecordsHint() {
		VariableString name = VariableString.newInstance("hint::%{_k}%", StringMode.VARIABLE_NAME);
		assertNotNull(name);

		data.enterScope();
		try {
			assertEquals("hint::key", name.toString(event));
			assertArrayEquals(new Class<?>[] {String.class}, data.get("hint::key"));
		} finally {
			data.exitScope();
		}
	}

	@Test
	public void localNameInsideAGlobalsScopeRecordsNoHint() {
		Variable<?> local = Variable.newInstance("_hint::%{_k}%", new Class[] {Object.class});
		assertNotNull(local);
		assertTrue(local.isLocal());

		// as when the local is evaluated while building the name of a global, e.g. {hint::%{_hint::%{_k}%}%}
		data.enterScope();
		try {
			local.getRaw(event);
			assertNull("a local's name recorded a hint in the enclosing scope", data.get("hint::key"));
		} finally {
			data.exitScope();
		}
	}

}
