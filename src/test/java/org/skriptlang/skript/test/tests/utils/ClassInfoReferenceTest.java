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
package org.skriptlang.skript.test.tests.utils;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.skript.variables.Variables;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;


public class ClassInfoReferenceTest {

	@NotNull
	private Expression<ClassInfoReference> parseAndWrap(String expr) {
		ParseResult parseResult = SkriptParser.parse(expr, "%classinfos%", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
		if (parseResult == null)
			throw new IllegalStateException("Parsed expression " + expr + " is null");
		return ClassInfoReference.wrap((Expression<ClassInfo<?>>) parseResult.exprs[0]);
	}

	@Test
	public void testWrapper() {
		ClassInfoReference reference = parseAndWrap("object").getSingle(null);
        Assert.assertEquals(Object.class, reference.getClassInfo().getC());
		Assert.assertTrue(reference.isPlural().isFalse());

		reference = parseAndWrap("string").getSingle(null);
		Assert.assertEquals(String.class, reference.getClassInfo().getC());
		Assert.assertTrue(reference.isPlural().isFalse());

		reference = parseAndWrap("players").getSingle(null);
		Assert.assertEquals(Player.class, reference.getClassInfo().getC());
		Assert.assertTrue(reference.isPlural().isTrue());

		Event event = ContextlessEvent.get();
		Variables.setVariable("classinfo", Classes.getExactClassInfo(Block.class), event, true);
		reference = parseAndWrap("{_classinfo}").getSingle(event);
		Assert.assertEquals(Block.class, reference.getClassInfo().getC());
		Assert.assertTrue(reference.isPlural().isUnknown());

		ExpressionList<ClassInfoReference> referenceList = (ExpressionList<ClassInfoReference>) parseAndWrap("blocks, player or entities");
        Assert.assertFalse(referenceList.getAnd());
		Expression<? extends ClassInfoReference>[] childExpressions = referenceList.getExpressions();

		ClassInfoReference firstReference = childExpressions[0].getSingle(null);
		Assert.assertEquals(Block.class, firstReference.getClassInfo().getC());
		Assert.assertTrue(firstReference.isPlural().isTrue());

		ClassInfoReference secondReference = childExpressions[1].getSingle(null);
		Assert.assertEquals(Player.class, secondReference.getClassInfo().getC());
		Assert.assertTrue(secondReference.isPlural().isFalse());

		ClassInfoReference thirdReference = childExpressions[2].getSingle(null);
		Assert.assertEquals(Entity.class, thirdReference.getClassInfo().getC());
		Assert.assertTrue(thirdReference.isPlural().isTrue());

		referenceList = (ExpressionList<ClassInfoReference>) parseAndWrap("{_block} and {_player}");
		Assert.assertTrue(referenceList.getAnd());
		childExpressions = referenceList.getExpressions();

		event = ContextlessEvent.get();
		Variables.setVariable("block", Classes.getExactClassInfo(Block.class), event, true);
		Variables.setVariable("player", Classes.getExactClassInfo(Player.class), event, true);
		firstReference = childExpressions[0].getSingle(event);
		Assert.assertEquals(Block.class, firstReference.getClassInfo().getC());
		Assert.assertTrue(firstReference.isPlural().isUnknown());

		secondReference = childExpressions[1].getSingle(event);
		Assert.assertEquals(Player.class, secondReference.getClassInfo().getC());
		Assert.assertTrue(secondReference.isPlural().isUnknown());

	}

}
