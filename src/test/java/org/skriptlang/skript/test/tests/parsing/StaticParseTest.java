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
package org.skriptlang.skript.test.tests.parsing;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.junit.Assert;
import org.junit.Test;

public class StaticParseTest {

	@Test
	public void testMultipleSingleExpressions() {
		ParseResult result = SkriptParser.parse("1 test 2 test 3", "%number% test %number% test %number%", SkriptParser.ALL_FLAGS, ParseContext.COMMAND);
		Assert.assertNotNull("parse method returned null", result);
		Assert.assertEquals("parse method returned wrong number of expressions", 3, result.exprs.length);
		Assert.assertEquals("parse method returned wrong first expression", 1L, result.exprs[0].getSingle(null));
		Assert.assertEquals("parse method returned wrong second expression", 2L, result.exprs[1].getSingle(null));
		Assert.assertEquals("parse method returned wrong first expression", 3L, result.exprs[2].getSingle(null));
	}

	@Test
	public void testMultipleMultipleExpressions() {
		ParseResult result = SkriptParser.parse("1, 2 and 3 test 4, 5 and 6", "%numbers% test %numbers%", SkriptParser.ALL_FLAGS, ParseContext.COMMAND);
		Assert.assertNotNull("parse method returned null", result);
		Assert.assertEquals("parse method returned wrong number of expressions", 2, result.exprs.length);
		Assert.assertArrayEquals("parse method returned wrong first expression", new Long[]{1L,2L,3L}, result.exprs[0].getArray(null));
		Assert.assertArrayEquals("parse method returned wrong second expression", new Long[]{4L,5L,6L}, result.exprs[1].getArray(null));
	}

	@Test
	public void testMultipleMixedExpressions() {
		ParseResult result = SkriptParser.parse("1 test 2, 3 and 4", "%number% test %numbers%", SkriptParser.ALL_FLAGS, ParseContext.COMMAND);
		Assert.assertNotNull("parse method returned null", result);
		Assert.assertEquals("parse method returned wrong number of expressions", 2, result.exprs.length);
		Assert.assertEquals("parse method returned wrong first expression", 1L, result.exprs[0].getSingle(null));
		Assert.assertArrayEquals("parse method returned wrong second expression", new Long[]{2L,3L,4L}, result.exprs[1].getArray(null));
	}

	@Test
	public void testMultipleExpression() {
		ParseResult result = SkriptParser.parse("1, 2 and 3", "%numbers%", SkriptParser.ALL_FLAGS, ParseContext.COMMAND);
		Assert.assertNotNull("parse method returned null", result);
		Assert.assertEquals("parse method returned wrong number of expressions", 1, result.exprs.length);
		Assert.assertArrayEquals("parse method returned wrong first expression", new Long[]{1L,2L,3L}, result.exprs[0].getArray(null));
	}

	@Test
	public void testSingleExpression() {
		ParseResult result = SkriptParser.parse("1", "%number%", SkriptParser.ALL_FLAGS, ParseContext.COMMAND);
		Assert.assertNotNull("parse method returned null", result);
		Assert.assertEquals("parse method returned wrong number of expressions", 1, result.exprs.length);
		Assert.assertEquals("parse method returned wrong first expression", 1L, result.exprs[0].getSingle(null));
	}

}
