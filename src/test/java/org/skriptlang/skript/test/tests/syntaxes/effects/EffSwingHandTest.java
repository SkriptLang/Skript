package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffSwingHand;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.entity.LivingEntity;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class EffSwingHandTest extends SkriptJUnitTest {

	private LivingEntity testEntity;
	private Effect swingMainHandEffect;
	private Effect swingOffhandEffect;

	@Before
	public void setup() {
		testEntity = EasyMock.niceMock(LivingEntity.class);
		swingMainHandEffect = Effect.parse("make {_entity} swing their main hand", null);
		swingOffhandEffect = Effect.parse("make {_entity} swing their offhand", null);
	}

	@Test
	public void test() {
		if (!EffSwingHand.SWINGING_IS_SUPPORTED)
			return;
		if (swingMainHandEffect == null)
			Assert.fail("Main hand is null");
		if (swingOffhandEffect == null)
			Assert.fail("Offhand effect is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("entity", testEntity, event, true);

		testEntity.swingMainHand();
		EasyMock.expectLastCall();
		EasyMock.replay(testEntity);
		TriggerItem.walk(swingMainHandEffect, event);
		EasyMock.verify(testEntity);

		EasyMock.resetToNice(testEntity);
		testEntity.swingOffHand();
		EasyMock.expectLastCall();
		EasyMock.replay(testEntity);
		TriggerItem.walk(swingOffhandEffect, event);
		EasyMock.verify(testEntity);
	}

}
