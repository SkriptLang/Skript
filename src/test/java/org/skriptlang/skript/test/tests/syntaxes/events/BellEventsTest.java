/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pillager;
import org.bukkit.event.Event;
import org.bukkit.event.block.BellResonateEvent;
import org.bukkit.event.block.BellRingEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BellEventsTest extends SkriptJUnitTest {

	private static final boolean canRun = Skript.classExists("org.bukkit.block.Bell");

	private Block bell;
	private LivingEntity pillager;

	@Before
	public void setUp() {
		if (!canRun)
			return;
		this.bell = setBlock(Material.BELL);
		this.pillager = getTestWorld().spawn(bell.getLocation().add(0, 1, 0), Pillager.class);
		setShutdownDelay(1);
	}
	
	@Test
	public void testEvents() {
		if (!canRun)
			return;
		Set<Event> events = new HashSet<>();
		if (Skript.classExists("org.bukkit.event.block.BellRingEvent")) {
			events.add(new BellRingEvent(this.bell, BlockFace.EAST, null));
		} else if (Skript.classExists("io.papermc.paper.event.block.BellRingEvent")) {
			try {
				events.add(io.papermc.paper.event.block.BellRingEvent.class.getConstructor(Block.class, Entity.class)
					.newInstance(this.bell, null));
			} catch (ReflectiveOperationException ignored) {
			}

		}

		if (Skript.classExists("org.bukkit.event.block.BellResonateEvent"))
			events.add(new BellResonateEvent(this.bell, Collections.singletonList(this.pillager)));

		for (Event event : events)
			Bukkit.getPluginManager().callEvent(event);
	}

}
