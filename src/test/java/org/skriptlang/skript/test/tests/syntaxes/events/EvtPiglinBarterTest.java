package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EvtPiglinBarterTest extends SkriptJUnitTest {

	private Piglin piglin;

	static {
		setShutdownDelay(1);
	}

	@Before
	public void spawn() {
		piglin = spawnTestEntity(EntityType.PIGLIN);
	}

	@Test
	public void testCall() {
		ItemStack input = new ItemStack(Material.GOLD_INGOT);
		List<ItemStack> outcome = new ArrayList<>();
		outcome.add(new ItemStack(Material.EMERALD));

		Bukkit.getPluginManager().callEvent(new PiglinBarterEvent(piglin, input, outcome));
	}

	@After
	public void remove() {
		piglin.remove();
	}

}
