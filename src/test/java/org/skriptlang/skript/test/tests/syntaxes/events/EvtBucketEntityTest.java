package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Cod;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.entity.Salmon;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtBucketEntityTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Fish salmon;
	private Fish cod;

	@Before
	public void setup() {
		salmon = getTestLocation().getWorld().spawn(getTestLocation(), Salmon.class);
		cod = getTestLocation().getWorld().spawn(getTestLocation(), Cod.class);
	}

	@Test
	public void test() {
		Player player = EasyMock.niceMock(Player.class);
		EasyMock.expect(player.getName()).andReturn("Efnilite").anyTimes();
		EasyMock.replay(player);

		Bukkit.getPluginManager().callEvent(new PlayerBucketEntityEvent(
			player, salmon, new ItemStack(Material.WATER_BUCKET),
			new ItemStack(Material.SALMON_BUCKET), EquipmentSlot.HAND));

		Bukkit.getPluginManager().callEvent(new PlayerBucketEntityEvent(
			player, cod, new ItemStack(Material.WATER_BUCKET),
			new ItemStack(Material.COD_BUCKET), EquipmentSlot.HAND));
	}

	@After
	public void reset() {
		salmon.remove();
		cod.remove();
	}

}
