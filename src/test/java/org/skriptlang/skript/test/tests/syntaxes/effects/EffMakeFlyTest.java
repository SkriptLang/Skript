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

public class EffMakeFlyTest extends SkriptJUnitTest {

	private Player testPlayer;
	private Effect startFlyingEffect;
	private Effect stopFlyingEffect;

	@Before
	public void setup() {
		testPlayer = EasyMock.niceMock(Player.class);
		startFlyingEffect = Effect.parse("make {_player} start flying", null);
		stopFlyingEffect = Effect.parse("make {_player} stop flying", null);
	}

	@Test
	public void test() {
		if (startFlyingEffect == null)
			Assert.fail("Start flying effect is null");
		if (stopFlyingEffect == null)
			Assert.fail("Stop flying effect is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", testPlayer, event, true);

		testPlayer.setAllowFlight(true);
		EasyMock.expectLastCall();
		testPlayer.setFlying(true);
		EasyMock.expectLastCall();
		EasyMock.replay(testPlayer);
		TriggerItem.walk(startFlyingEffect, event);
		EasyMock.verify(testPlayer);

		EasyMock.resetToNice(testPlayer);
		testPlayer.setAllowFlight(false);
		EasyMock.expectLastCall();
		testPlayer.setFlying(false);
		EasyMock.expectLastCall();
		EasyMock.replay(testPlayer);
		TriggerItem.walk(stopFlyingEffect, event);
		EasyMock.verify(testPlayer);
	}

}
