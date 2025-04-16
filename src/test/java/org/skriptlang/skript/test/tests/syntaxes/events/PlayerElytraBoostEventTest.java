package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;

public class PlayerElytraBoostEventTest extends SkriptJUnitTest {

	private Player player;
	private Firework firework;

	@Before
	public void setUp() {
		player = EasyMock.niceMock(Player.class);
		EntityType entityType = EntityType.valueOf("FIREWORK");
		if (entityType == null) {
			entityType = EntityType.valueOf("FIREWORK_ROCKET");
		}
		assert entityType != null;
		firework = spawnTestEntity(entityType);
		firework.setTicksToDetonate(9999999);
	}

	@Test
	public void test() {
		Constructor<?> constructor = getConstructor(PlayerElytraBoostEvent.class, false, Player.class, ItemStack.class, Firework.class, EquipmentSlot.class);
		ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET);
		Event event;
		if (constructor != null) {
			event = newInstance(constructor, player, rocket, firework, EquipmentSlot.HAND);
		} else {
			constructor = getConstructor(PlayerElytraBoostEvent.class, Player.class, ItemStack.class, Firework.class);
			event = newInstance(constructor, player, rocket, firework);
		}

		Bukkit.getPluginManager().callEvent(event);
	}

	@After
	public void cleanUp() {
		if (firework != null)
			firework.remove();
	}

}
