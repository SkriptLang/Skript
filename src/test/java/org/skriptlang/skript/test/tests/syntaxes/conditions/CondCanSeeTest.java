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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CondCanSeeTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Player testPlayer;
	private Entity testEntity;
	private Condition canSeeCondition;
	private Set<UUID> hiddenEntities;

	@Before
	public void setup() {
		testPlayer = EasyMock.niceMock(Player.class);
		testEntity = spawnTestPig();
		canSeeCondition = Condition.parse("{_player} can see {_entity}", null);
		hiddenEntities = new HashSet<>();
	}

	@Test
	public void test() {
		if (Skript.getMinecraftVersion().isSmallerThan(new Version(1, 19)))
			return;
		if (canSeeCondition == null)
			Assert.fail("Hide entity effect is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", testPlayer, event, true);
		Variables.setVariable("entity", testEntity, event, true);

		// entity not in hiddenEntities
		EasyMock.expect(testPlayer.canSee(testEntity))
			.andAnswer(() -> !hiddenEntities.contains(((Entity) EasyMock.getCurrentArgument(0)).getUniqueId()));
		EasyMock.replay(testPlayer);
		assert canSeeCondition.check(event);
		EasyMock.verify(testPlayer);

		hiddenEntities.add(testEntity.getUniqueId());
		EasyMock.resetToNice(testPlayer);
		EasyMock.expect(testPlayer.canSee(testEntity))
			.andAnswer(() -> !hiddenEntities.contains(((Entity) EasyMock.getCurrentArgument(0)).getUniqueId()));
		EasyMock.replay(testPlayer);
		assert !canSeeCondition.check(event);
		EasyMock.verify(testPlayer);

		hiddenEntities.remove(testEntity.getUniqueId());
		EasyMock.resetToNice(testPlayer);
		EasyMock.expect(testPlayer.canSee(testEntity))
			.andAnswer(() -> !hiddenEntities.contains(((Entity) EasyMock.getCurrentArgument(0)).getUniqueId()));
		EasyMock.replay(testPlayer);
		assert canSeeCondition.check(event);
		EasyMock.verify(testPlayer);
	}

	@After
	public void removeEntity() {
		testEntity.remove();
	}

}
