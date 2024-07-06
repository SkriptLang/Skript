package org.skriptlang.skript.test.tests.syntaxes.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.util.Version;
import ch.njol.skript.variables.Variables;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CondCanSeeTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Player testPlayer;
	private Entity testEntity;
	private Condition canSeeCondition;

	@Before
	public void setup() {
		testPlayer = EasyMock.niceMock(Player.class);
		testEntity = spawnTestPig();
		canSeeCondition = Condition.parse("{_player} can see {_entity}", null);
	}

	@Test
	public void test() {
		if (Skript.getMinecraftVersion().isSmallerThan(new Version("1.19")))
			return;
		if (canSeeCondition == null)
			Assert.fail("Hide entity effect is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", testPlayer, event, true);
		Variables.setVariable("entity", testEntity, event, true);

		EasyMock.expect(testPlayer.canSee(testEntity)).andReturn(true);
		EasyMock.replay(testPlayer);
		assert canSeeCondition.check(event);
		EasyMock.verify(testPlayer);

		EasyMock.resetToNice(testPlayer);
		EasyMock.expect(testPlayer.canSee(testEntity)).andReturn(false);
		EasyMock.replay(testPlayer);
		assert !canSeeCondition.check(event);
		EasyMock.verify(testPlayer);

		EasyMock.resetToNice(testPlayer);
		EasyMock.expect(testPlayer.canSee(testEntity)).andReturn(true);
		EasyMock.replay(testPlayer);
		assert canSeeCondition.check(event);
		EasyMock.verify(testPlayer);
	}

	@After
	public void removeEntity() {
		testEntity.remove();
	}
}
