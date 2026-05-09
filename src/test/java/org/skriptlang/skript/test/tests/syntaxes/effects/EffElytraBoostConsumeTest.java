package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class EffElytraBoostConsumeTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	@Test
	public void test() {
		Player player = EasyMock.niceMock(Player.class);
		Firework firework = EasyMock.niceMock(Firework.class);

		ItemStack fireworkItemStack = new ItemStack(Material.FIREWORK_ROCKET);
		Assert.assertNotNull("Firework itemstack should not be null", fireworkItemStack);

		PlayerElytraBoostEvent event = new PlayerElytraBoostEvent(player,  fireworkItemStack,firework, EquipmentSlot.HAND);
		Bukkit.getPluginManager().callEvent(event);

		assert !event.shouldConsume() : "Firework should not be consumed";
	}

}
