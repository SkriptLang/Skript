package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EvtPiglinBarterTest extends SkriptJUnitTest {

	private Entity piglin;
	private static final boolean canRun = Skript.classExists("org.bukkit.event.entity.PiglinBarterEvent");

	static {
		setShutdownDelay(1);
	}

	@Before
	public void spawn() {
		if (!canRun)
			return;

		piglin = getTestWorld().spawnEntity(getTestLocation(), EntityType.PIGLIN);
	}

	@Test
	public void testCall() {
		if (!canRun)
			return;

		ItemStack input = new ItemStack(Material.GOLD_INGOT);
		List<ItemStack> outcome = new ArrayList<>();
		outcome.add(new ItemStack(Material.EMERALD));

		try {
			Bukkit.getPluginManager().callEvent(
				new org.bukkit.event.entity.PiglinBarterEvent(
					(org.bukkit.entity.Piglin) piglin, input, outcome));
		} catch (NoClassDefFoundError ignored) { }
	}

	@After
	public void remove() {
		if (!canRun)
			return;

		piglin.remove();
	}

}
