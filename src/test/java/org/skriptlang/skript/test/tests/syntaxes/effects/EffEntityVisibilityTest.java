package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
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

public class EffEntityVisibilityTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Player testPlayer;
	private Entity testEntity;
	private Effect hideEntityEffect;
	private Effect revealEntityEffect;

	@Before
	public void setup() {
		testPlayer = EasyMock.niceMock(Player.class);
		testEntity = spawnTestPig();
		hideEntityEffect = Effect.parse("hide {_entity} from {_player}", null);
		revealEntityEffect = Effect.parse("reveal {_entity} to {_player}", null);
	}

	@Test
	public void test() {
		if (Skript.getMinecraftVersion().isSmallerThan(new Version(1, 19)))
			return;
		if (hideEntityEffect == null)
			Assert.fail("Hide entity effect is null");
		if (revealEntityEffect == null)
			Assert.fail("Reveal player effect is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", testPlayer, event, true);
		Variables.setVariable("entity", testEntity, event, true);

		testPlayer.hideEntity(Skript.getInstance(), testEntity);
		EasyMock.expectLastCall();
		EasyMock.replay(testPlayer);
		TriggerItem.walk(hideEntityEffect, event);
		EasyMock.verify(testPlayer);

		EasyMock.resetToNice(testPlayer);
		testPlayer.showEntity(Skript.getInstance(), testEntity);
		EasyMock.expectLastCall();
		EasyMock.replay(testPlayer);
		TriggerItem.walk(revealEntityEffect, event);
		EasyMock.verify(testPlayer);
	}

	@After
	public void removeEntity() {
		testEntity.remove();
	}
}
