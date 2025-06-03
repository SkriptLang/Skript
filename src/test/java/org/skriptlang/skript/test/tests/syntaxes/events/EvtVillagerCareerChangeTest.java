package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent.ChangeReason;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtVillagerCareerChangeTest extends SkriptJUnitTest {

	private Villager villager;

	@Before
	public void setup() {
		villager = (Villager) getTestWorld().spawnEntity(getTestLocation(), EntityType.VILLAGER);
		villager.setProfession(Profession.CLERIC);
	}

	@Test
	public void test() {
		VillagerCareerChangeEvent event = new VillagerCareerChangeEvent(villager, Profession.ARMORER, ChangeReason.EMPLOYED);
		Bukkit.getPluginManager().callEvent(event);
	}

	@After
	public void cleanup() {
		if (villager != null)
			villager.remove();
	}

}
