package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtFertilizeEggTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Frog mother;
	private Frog father;
	private Player player;

	@Before
	public void before() {
		mother = spawnTestEntity(EntityType.FROG);
		father = spawnTestEntity(EntityType.FROG);

		player = EasyMock.niceMock(Player.class);
		EasyMock.replay(player);
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(
			new EntityFertilizeEggEvent(mother, father, player, new ItemStack(Material.SLIME_BALL), 5));
	}

	@After
	public void after() {
		mother.remove();
		father.remove();
	}

}
