package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtVehicleEnterTest extends SkriptJUnitTest {

	private Entity pig;
	private Player player;

	@Before
	public void setup() {
		pig = spawnTestPig();
		player = EasyMock.niceMock(Player.class);
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(new VehicleEnterEvent((Vehicle) pig, player));
	}

	@After
	public void cleanup() {
		if (pig != null)
			pig.remove();
	}

}
