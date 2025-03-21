package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.bukkit.input.InputKey;
import org.skriptlang.skript.test.utils.InputHelper;

public class ExprCurrentInputKeysTest extends SkriptJUnitTest {

	private static final boolean SUPPORTS_INPUT = Skript.classExists("org.bukkit.Input");

	static {
		setShutdownDelay(1);
	}

	private Player player;
	private Expression<? extends InputKey> inputKeyExpression;

	@Before
	public void setup() {
		if (!SUPPORTS_INPUT)
			return;
		player = EasyMock.niceMock(Player.class);
		//noinspection unchecked
		inputKeyExpression = new SkriptParser("input keys of {_player}").parseExpression(InputKey.class);
	}

	@Test
	public void test() {
		if (!SUPPORTS_INPUT)
			return;
		if (inputKeyExpression == null)
			Assert.fail("Input keys expression is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", player, event, true);

		EasyMock.expect(player.getCurrentInput()).andReturn(InputHelper.fromKeys(InputKey.FORWARD, InputKey.JUMP));
		EasyMock.replay(player);
		InputKey[] keys = inputKeyExpression.getArray(event);
		Assert.assertArrayEquals(keys, new InputKey[]{InputKey.FORWARD, InputKey.JUMP});
		EasyMock.verify(player);
	}

}
