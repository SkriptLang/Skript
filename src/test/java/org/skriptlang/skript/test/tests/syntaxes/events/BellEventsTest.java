package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pillager;
import org.bukkit.event.Event;
import org.bukkit.event.block.BellResonateEvent;
import org.bukkit.event.block.BellRingEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BellEventsTest extends SkriptJUnitTest {

	private Block bell;
	private LivingEntity pillager;

	@Before
	public void setUp() {
		this.bell = setBlock(Material.BELL);
		this.pillager = getTestWorld().spawn(bell.getLocation().add(0, 1, 0), Pillager.class);
		setShutdownDelay(1);
	}
	
	@Test
	public void testEvents() {
		Set<Event> events = new HashSet<>();
		events.add(new BellRingEvent(this.bell, BlockFace.EAST, null));
		events.add(new BellResonateEvent(this.bell, Collections.singletonList(this.pillager)));
		for (Event event : events)
			Bukkit.getPluginManager().callEvent(event);
	}

}
