package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class EffMakeEggHatchTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	@Test
	public void test() {
		Player player = EasyMock.niceMock(Player.class);
		Egg egg = EasyMock.niceMock(Egg.class);
		PlayerEggThrowEvent event = new PlayerEggThrowEvent(player, egg, false, (byte) 0, EntityType.CHICKEN);
		Bukkit.getPluginManager().callEvent(event);

		assert event.isHatching() : "Egg should be hatching";
	}

}
