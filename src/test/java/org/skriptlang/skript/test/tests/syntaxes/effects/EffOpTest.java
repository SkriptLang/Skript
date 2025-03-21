package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EffOpTest extends SkriptJUnitTest {

	private Player testPlayer;
	private Effect opPlayerEffect;
	private Effect deopPlayerEffect;

	@Before
	public void setup() {
		testPlayer = EasyMock.niceMock(Player.class);
		opPlayerEffect = Effect.parse("op {_player}", null);
		deopPlayerEffect = Effect.parse("deop {_player}", null);
	}

	@Test
	public void test() {
		if (opPlayerEffect == null)
			Assert.fail("Op player effect is null");
		if (deopPlayerEffect == null)
			Assert.fail("Deop player effect is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", testPlayer, event, true);

		testPlayer.setOp(true);
		EasyMock.expectLastCall();
		EasyMock.replay(testPlayer);
		TriggerItem.walk(opPlayerEffect, event);
		EasyMock.verify(testPlayer);

		EasyMock.resetToNice(testPlayer);
		testPlayer.setOp(false);
		EasyMock.expectLastCall();
		EasyMock.replay(testPlayer);
		TriggerItem.walk(deopPlayerEffect, event);
		EasyMock.verify(testPlayer);
	}

}
