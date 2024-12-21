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
		firework = (Firework) getTestWorld().spawnEntity(getTestLocation(), entityType);
		firework.setTicksToDetonate(9999999);
	}

	@Test
	public void test() {
		Constructor<?> constructor = null;
		boolean newerConstructor = false;
		try {
			constructor = PlayerElytraBoostEvent.class.getConstructor(Player.class, ItemStack.class, Firework.class, EquipmentSlot.class);
			newerConstructor = true;
		} catch (Exception ignored) {
			try {
				constructor = PlayerElytraBoostEvent.class.getConstructor(Player.class, ItemStack.class, Firework.class);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("No valid constructor for 'PlayerElytraBoostEvent'");
			}
		}

		try {
			Event event;
			if (newerConstructor) {
				event = (Event) constructor.newInstance(player, new ItemStack(Material.FIREWORK_ROCKET), firework, EquipmentSlot.HAND);
			} else {
				event = (Event) constructor.newInstance(player, new ItemStack(Material.FIREWORK_ROCKET), firework);
			}

			Bukkit.getPluginManager().callEvent(event);
		} catch (Exception e) {
			throw new RuntimeException("Unable to construct event.");
		}
	}

	@After
	public void cleanUp() {
		if (firework != null)
			firework.remove();
	}

}
