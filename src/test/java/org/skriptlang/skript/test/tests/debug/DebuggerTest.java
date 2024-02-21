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
package org.skriptlang.skript.test.tests.debug;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import ch.njol.util.OpenCloseable;
import org.bukkit.event.Event;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.lang.debug.Debugger;
import org.skriptlang.skript.lang.debug.Debuggers;
import org.skriptlang.skript.lang.script.Script;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class DebuggerTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private final Path scriptPath = Paths.get("..", "..", "..", "src", "test", "skript", "junit", "DebuggerTest.sk")
		.toAbsolutePath().normalize();

	private final Map<TriggerItem, Integer> lineMap = new WeakHashMap<>();

	private final Map<Integer, Consumer<? super Event>> actionMap = new HashMap<>();

	final Debugger debugger = new Debugger() {

		@Override
		public void onParse(SyntaxElement element) {
			if (!(element instanceof TriggerItem))
				return;
			TriggerItem triggerItem = (TriggerItem) element;
			Node node = ParserInstance.get().getNode();
			if (node == null)
				return;
            lineMap.put(triggerItem, node.getLine());
        }

		@Override
		public void onWalk(TriggerItem triggerItem, Event event) {
			if (checkFile(triggerItem)) {
				final Consumer<? super Event> action = actionMap.get(lineMap.get(triggerItem));
				if (action != null)
					action.accept(event);
			}
		}

		private boolean checkFile(TriggerItem triggerItem) {
			Trigger trigger = triggerItem.getTrigger();
			if (trigger == null)
				return false;
			Script script = trigger.getScript();
			if (script == null)
				return false;
			Path path = script.getConfig().getPath();
            return scriptPath.equals(path);
        }

	};

	private void setBreakPoint(int line, Consumer<? super Event> action) {
		actionMap.put(line, action);
	}

	@Before
	public void attach() {
		Debuggers.attachDebugger(debugger);
	}

	@Test
	public void test() throws ExecutionException, InterruptedException {
		Script script = ScriptLoader.getScript(scriptPath.toFile());
		if (script == null)
			Assert.fail("Script is not loaded");
        ScriptLoader.reloadScript(script, OpenCloseable.EMPTY).get();  // wait for reload
        Object[] variableStates = new Object[3];
		setBreakPoint(6, event -> variableStates[0] = Variables.getVariable("test", event, true));
		setBreakPoint(7, event -> variableStates[1] = Variables.getVariable("test", event, true));
		setBreakPoint(8, event -> variableStates[2] = Variables.getVariable("test", event, true));
        String mainFunctionName = "DebuggerTest_main";
		Function<?> mainFunction = Functions.getGlobalFunction(mainFunctionName);
		if (!Debuggers.enabled())
			Assert.fail("Debugger is not enabled");
		if (mainFunction == null)
			Assert.fail("Global function " + mainFunctionName + " was not found");
		mainFunction.execute(new Object[0][]);
		Assert.assertArrayEquals(new Object[] { null, 1L, 2L }, variableStates);
	}

	@After
	public void detach() {
		Debuggers.detachDebugger(debugger);
	}

}
