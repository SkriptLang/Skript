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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class EffFeedTest extends SkriptJUnitTest {

	private Player easyMockPlayer;
	private Effect feedFullyEffect;
	private Effect feedPartiallyEffect;

	@Before
	public void setup() {
		easyMockPlayer = EasyMock.niceMock(Player.class);
		feedFullyEffect = Effect.parse("feed {_player}", null);
		feedPartiallyEffect = Effect.parse("feed {_player} by {_amount} beef", null);
	}

	@Test
	public void test() {
		if (feedFullyEffect == null)
			Assert.fail("Fully effect is null");
		if (feedPartiallyEffect == null)
			Assert.fail("Partially effect is null");

		int amountToFeed = 1;
		int maxFoodLevel = 20;
		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", getMockPlayer(), event, true);
		Variables.setVariable("amount", amountToFeed, event, true);

		easyMockPlayer.setFoodLevel(EasyMock.eq(maxFoodLevel));
		EasyMock.expectLastCall();
		EasyMock.replay(easyMockPlayer);
		TriggerItem.walk(feedFullyEffect, event);
		EasyMock.verify(easyMockPlayer);

		EasyMock.resetToNice(easyMockPlayer);
		easyMockPlayer.setFoodLevel(EasyMock.eq(amountToFeed));
		EasyMock.expectLastCall();
		EasyMock.replay(easyMockPlayer);
		TriggerItem.walk(feedPartiallyEffect, event);
		EasyMock.verify(easyMockPlayer);
	}

	private Player getMockPlayer() {
		InvocationHandler handler = (proxy, method, args) -> {
			if (method.getName().equals("getFoodLevel"))
				return 0;
			return method.invoke(easyMockPlayer, args);
		};
		return (Player) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Player.class }, handler);
	}

}
