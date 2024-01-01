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
