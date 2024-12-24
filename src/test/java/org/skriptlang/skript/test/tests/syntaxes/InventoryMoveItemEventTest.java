package org.skriptlang.skript.test.tests.syntaxes;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

// TODO properly add test after merge of https://github.com/SkriptLang/Skript/pull/6261
public class InventoryMoveItemEventTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	@Test
	public void test() {
		Inventory chestInventory = Bukkit.createInventory(null, InventoryType.CHEST);
		ItemStack itemStack = new ItemStack(Material.STONE);
		Inventory hopperInventory = Bukkit.createInventory(null, InventoryType.HOPPER);
		Bukkit.getPluginManager().callEvent(new InventoryMoveItemEvent(chestInventory, itemStack, hopperInventory, false));
	}

}
