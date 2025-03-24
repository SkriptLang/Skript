package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.VaultDisplayItemEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;

public class EvtVaultDisplayItemTest extends SkriptJUnitTest {

	private boolean VAULT_EVENT_EXISTS = Skript.classExists("org.bukkit.event.block.VaultDisplayItemEvent");
	private Block vault;
	private final ItemStack item = new ItemStack(Material.DIAMOND);

	@Before
	public void setup() {
		if (!VAULT_EVENT_EXISTS)
			return;
		vault = setBlock(Material.VAULT);
	}

	@Test
	public void test() {
		if (!VAULT_EVENT_EXISTS)
			return;
		VaultDisplayItemEvent event = new VaultDisplayItemEvent(vault, item);
		Bukkit.getPluginManager().callEvent(event);
	}

}
