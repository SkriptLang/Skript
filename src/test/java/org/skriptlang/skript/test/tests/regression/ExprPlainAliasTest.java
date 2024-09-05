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
package org.skriptlang.skript.test.tests.regression;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// https://github.com/SkriptLang/Skript/issues/5994
public class ExprPlainAliasTest extends SkriptJUnitTest {
	private ItemType itemType;
	@Nullable
	private Effect getPlainRandomItemEffect;

	@Before
	public void setup() {
		itemType = EasyMock.niceMock(ItemType.class);
		getPlainRandomItemEffect = Effect.parse("set {_a} to plain {_item}", null);
	}

	@Test
	public void test() {
		if (getPlainRandomItemEffect == null)
			Assert.fail("Plain item effect is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("item", itemType, event, true);

		EasyMock.expect(itemType.getMaterial()).andReturn(Material.STONE).atLeastOnce();
		EasyMock.replay(itemType);
		TriggerItem.walk(getPlainRandomItemEffect, event);
		EasyMock.verify(itemType);

	}

}
