package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtFishTest extends SkriptJUnitTest {

	private Fish salmon;
	private Fish cod;
	private FishHook hook;

	@Before
	public void setup() {
		salmon = getTestLocation().getWorld().spawn(getTestLocation(), Salmon.class);
		cod = getTestLocation().getWorld().spawn(getTestLocation(), Cod.class);
		hook = cod.launchProjectile(FishHook.class);
	}

	@Test
	public void test() {
		Player player = EasyMock.niceMock(Player.class);
		EasyMock.expect(player.getName()).andReturn("Efnilite").anyTimes();
		EasyMock.replay(player);

		hook.setHookedEntity(salmon);
		Bukkit.getPluginManager().callEvent(new PlayerFishEvent(
			player, salmon, hook, PlayerFishEvent.State.CAUGHT_FISH));

		hook.setHookedEntity(cod);
		Bukkit.getPluginManager().callEvent(new PlayerFishEvent(
			player, cod, hook, PlayerFishEvent.State.FISHING));
	}

	@After
	public void reset() {
		salmon.remove();
		cod.remove();
		hook.remove();
	}

}
