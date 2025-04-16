package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;

public class EvtVaultDisplayItemTest extends SkriptJUnitTest {

	private static final boolean VAULT_EVENT_EXISTS = Skript.classExists("org.bukkit.event.block.VaultDisplayItemEvent");
	private Block vault;
	private final ItemStack item = new ItemStack(Material.DIAMOND);

	@Before
	public void setup() {
		if (!VAULT_EVENT_EXISTS)
			return;
		vault = setBlock(Material.valueOf("VAULT"));
	}

	@Test
	public void test() {
		if (!VAULT_EVENT_EXISTS)
			return;
		Event event = null;
        try {
            Class<?> eventClass = Class.forName("org.bukkit.event.block.VaultDisplayItemEvent");
			Constructor<?> constructor = eventClass.getConstructor(Block.class, ItemStack.class);
			event = (Event) constructor.newInstance(vault, item);
        } catch (Exception ignored) {}
		assert event != null;
		Bukkit.getPluginManager().callEvent(event);
	}

}
