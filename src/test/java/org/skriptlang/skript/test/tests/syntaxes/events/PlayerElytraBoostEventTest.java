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
		ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET);
		Constructor<?> constructor = null;
		Event event = null;
		try {
			constructor = PlayerElytraBoostEvent.class.getDeclaredConstructor(Player.class, ItemStack.class, Firework.class, EquipmentSlot.class);
			event = (Event) constructor.newInstance(player, rocket, firework, EquipmentSlot.HAND);
		} catch (Exception ignored) {}
		if (constructor == null) {
            try {
                constructor = PlayerElytraBoostEvent.class.getConstructor(Player.class, ItemStack.class, Firework.class);
				event = (Event) constructor.newInstance(player, rocket, firework);
			} catch (Exception e) {
				throw new RuntimeException("No valid constructor for 'PlayerElytraBoostEvent'");
			}
		}
		assert event != null;

		Bukkit.getPluginManager().callEvent(event);
	}

	@After
	public void cleanUp() {
		if (firework != null)
			firework.remove();
	}

}
