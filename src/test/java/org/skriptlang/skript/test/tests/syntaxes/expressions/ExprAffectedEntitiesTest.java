package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExprAffectedEntitiesTest extends SkriptJUnitTest {

	private AreaEffectCloud cloud;
	private Pig piggy;
	private final List<LivingEntity> entityList = new ArrayList<>();

	@Before
	public void setUp() {
		piggy = spawnTestPig();
		entityList.add(piggy);
		cloud = (AreaEffectCloud) getTestWorld().spawnEntity(getTestLocation(), EntityType.AREA_EFFECT_CLOUD);
	}

	@Test
	public void callEvent() {
		AreaEffectCloudApplyEvent event = new AreaEffectCloudApplyEvent(cloud, entityList);
		Bukkit.getPluginManager().callEvent(event);
	}

	@After
	public void cleanUp() {
		if (piggy != null)
			piggy.remove();
		if (cloud != null)
			cloud.remove();
	}

}
