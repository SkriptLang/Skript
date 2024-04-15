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
package org.skriptlang.skript.test.tests.lang;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Pig;
import org.bukkit.event.entity.EntityDeathEvent;
import org.junit.Test;

import java.util.ArrayList;

public class CancelledEventsTest extends SkriptJUnitTest {


	static {
		setShutdownDelay(1);
	}

	@Test
	public void callCancelledEvent() {
		Pig pig = spawnTestPig();
		EntityDeathEvent event = new EntityDeathEvent(pig, new ArrayList<>());

		// call cancelled event
		event.setCancelled(true);
		Bukkit.getPluginManager().callEvent(event);

		// call non-cancelled event
		event.setCancelled(false);
		Bukkit.getPluginManager().callEvent(event);
	}

}

