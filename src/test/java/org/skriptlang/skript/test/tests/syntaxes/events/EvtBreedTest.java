package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtBreedTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Pig child;
	private Pig mother;
	private Pig father;
	private Player player;

	@Before
	public void before() {
		child = spawnTestPig();
		child.setCustomName("child");
		mother = spawnTestPig();
		mother.setCustomName("mother");
		father = spawnTestPig();
		father.setCustomName("father");

		player = EasyMock.niceMock(Player.class);
		EasyMock.expect(player.getName()).andReturn("Efnilite");
		EasyMock.replay(player);
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(
			new org.bukkit.event.entity.EntityBreedEvent(
				child, mother, father, player, new ItemStack(Material.CARROT), 0));
	}

	@After
	public void after() {
		child.remove();
		mother.remove();
		father.remove();
	}

}
