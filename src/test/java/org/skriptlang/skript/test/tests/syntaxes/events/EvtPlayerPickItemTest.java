package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.sections.EffSecSpawn;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class EvtPlayerPickItemTest extends SkriptJUnitTest {

	private static final boolean SUPPORTS_PICK_EVENT = Skript.classExists("io.papermc.paper.event.player.PlayerPickBlockEvent");

	private Player player;
	private Pig pickedEntity;
	private Block pickedBlock;

	@Before
	public void setUp() {
		if (!SUPPORTS_PICK_EVENT)
			return;
		player = EasyMock.niceMock(Player.class);
		pickedEntity = spawnTestPig();
		pickedBlock = setBlock(Material.DIRT);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	public void test() {
		if (!SUPPORTS_PICK_EVENT)
			return;
		Event pickBlockEvent = createPickBlockEvent();
		Event pickEntityEvent = createPickEntityEvent();
		Bukkit.getPluginManager().callEvent(pickBlockEvent);

		Entity previous = EffSecSpawn.lastSpawned;
		EffSecSpawn.lastSpawned = pickedEntity;
		Bukkit.getPluginManager().callEvent(pickEntityEvent);
		EffSecSpawn.lastSpawned = previous;
	}

	private Event createPickBlockEvent() {
		Event event = null;
		try {
			Class<?> eventClass = Class.forName("io.papermc.paper.event.player.PlayerPickBlockEvent");
			event = (Event) eventClass.getConstructor(Player.class, Block.class, boolean.class, int.class, int.class)
					.newInstance(player, pickedBlock, true, 0, 0);
		} catch (Exception ignored) {}
		return event;
	}

	private Event createPickEntityEvent() {
		Event event = null;
		try {
			Class<?> eventClass = Class.forName("io.papermc.paper.event.player.PlayerPickEntityEvent");
			event = (Event) eventClass.getConstructor(Player.class, Entity.class, boolean.class, int.class, int.class)
					.newInstance(player, pickedEntity, true, 0, 0);
		} catch (Exception ignored) {}
		return event;
	}

}
