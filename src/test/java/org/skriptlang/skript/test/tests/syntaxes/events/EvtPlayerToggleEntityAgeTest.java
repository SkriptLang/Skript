package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class EvtPlayerToggleEntityAgeTest extends SkriptJUnitTest {

	private static final boolean SUPPORTS_PLAYER_TOGGLE_ENTITY_AGE = Skript.classExists("io.papermc.paper.event.player.PlayerToggleEntityAgeLockEvent");

	private Player player;
	private Cow entity;
	private ItemStack item;

	@Before
	public void setUp() {
		if (!SUPPORTS_PLAYER_TOGGLE_ENTITY_AGE)
			return;
		player = EasyMock.niceMock(Player.class);
		entity = EasyMock.niceMock(Cow.class);
		item = new ItemStack(Material.GOLDEN_DANDELION);
	}

	@Test
	public void testLock() {
		if (!SUPPORTS_PLAYER_TOGGLE_ENTITY_AGE)
			return;
		callEvent(true);
	}

	@Test
	public void testUnlock() {
		if (!SUPPORTS_PLAYER_TOGGLE_ENTITY_AGE)
			return;
		callEvent(false);
	}

	private void callEvent(boolean ageLocked) {
		try {
			Class<?> eventClass = Class.forName("io.papermc.paper.event.player.PlayerToggleEntityAgeLockEvent");
			Event event = (Event) eventClass.getConstructor(Player.class, LivingEntity.class, ItemStack.class, EquipmentSlot.class, boolean.class)
				.newInstance(player, entity, item, EquipmentSlot.HAND, ageLocked);
			Bukkit.getPluginManager().callEvent(event);
		} catch (Exception ignored) {}
	}

}
