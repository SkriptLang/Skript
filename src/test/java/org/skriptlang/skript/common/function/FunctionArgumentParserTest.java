package org.skriptlang.skript.common.function;

import org.junit.Test;
import org.skriptlang.skript.common.function.FunctionArgumentParser;
import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;

import static org.junit.Assert.assertEquals;

public class FunctionArgumentParserTest {

	@Test
	public void testUnnamedArgs() {
		Argument<String>[] arguments = new FunctionArgumentParser("1, 2, \"hey:, gi:rl\", ({forza, real::*}, {_x::2}, 2)").getArguments();

		assertEquals(4, arguments.length);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "1"), arguments[0]);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "2"), arguments[1]);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "\"hey:, gi:rl\""), arguments[2]);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "({forza, real::*}, {_x::2}, 2)"), arguments[3]);

		arguments = new FunctionArgumentParser("1, 2, \"hey, girl\", ({forza, real}, 2)").getArguments();

		assertEquals(4, arguments.length);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "1"), arguments[0]);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "2"), arguments[1]);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "\"hey, girl\""), arguments[2]);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "({forza, real}, 2)"), arguments[3]);
	}

	@Test
	public void testNamedArgs() {
		Argument<String>[] arguments = new FunctionArgumentParser("a_rg: 1, 2, womp: \"hey:, gi:rl\", list: ({forza, real::*}, {_x::2}, 2)").getArguments();

		assertEquals(4, arguments.length);
		assertEquals(new Argument<>(ArgumentType.NAMED, "a_rg", "1"), arguments[0]);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "2"), arguments[1]);
		assertEquals(new Argument<>(ArgumentType.NAMED, "womp", "\"hey:, gi:rl\""), arguments[2]);
		assertEquals(new Argument<>(ArgumentType.NAMED, "list", "({forza, real::*}, {_x::2}, 2)"), arguments[3]);

		arguments = new FunctionArgumentParser("2: 1, 2, 3_60: \"hey, girl\", 1list: ({forza, real}, 2)").getArguments();

		assertEquals(4, arguments.length);
		assertEquals(new Argument<>(ArgumentType.NAMED, "2", "1"), arguments[0]);
		assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "2"), arguments[1]);
		assertEquals(new Argument<>(ArgumentType.NAMED, "3_60", "\"hey, girl\""), arguments[2]);
		assertEquals(new Argument<>(ArgumentType.NAMED, "1list", "({forza, real}, 2)"), arguments[3]);
	}

	@Test
	public void testSingleNamedList() {
		Argument<String>[] arguments = new FunctionArgumentParser("1: (2, 3, 4)").getArguments();

		assertEquals(1, arguments.length);
		assertEquals(new Argument<>(ArgumentType.NAMED, "1", "(2, 3, 4)"), arguments[0]);
	}

	@Test
	public void testStringEscape() {
		Argument<String>[] arguments = new FunctionArgumentParser("1: \"hello \"\" %{x,y::%player's car, or not!%::*} there\"\"\"").getArguments();

		assertEquals(1, arguments.length);
		assertEquals(new Argument<>(ArgumentType.NAMED, "1", "\"hello \"\" %{x,y::%player's car, or not!%::*} there\"\"\""), arguments[0]);
	}

	@Test
	public void testStringTimeFormat() {
		{
			Argument<String>[] arguments = new FunctionArgumentParser("\"%now formatted as \"HH:mm:ss\"%\"").getArguments();

			assertEquals(1, arguments.length);
			assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "\"%now formatted as \"HH:mm:ss\"%\""), arguments[0]);
		}
		{
			Argument<String>[] arguments = new FunctionArgumentParser("\"%now formatted as \"\"HH:mm:ss\"\"%\"").getArguments();

			assertEquals(1, arguments.length);
			assertEquals(new Argument<>(ArgumentType.UNNAMED, null, "\"%now formatted as \"\"HH:mm:ss\"\"%\""), arguments[0]);
		}
	}

	@Test
	public void testFullyQualifiedNames() {
		{
			Argument<String>[] arguments = new FunctionArgumentParser("minecraft: minecraft:air").getArguments();

			assertEquals(1, arguments.length);
			assertEquals(new Argument<>(ArgumentType.NAMED, "minecraft", "minecraft:air"), arguments[0]);
			assertEquals("minecraft: minecraft:air", arguments[0].raw());
		}

		{
			Argument<String>[] arguments = new FunctionArgumentParser("minecraft:minecraft:air").getArguments();

			assertEquals(1, arguments.length);
			assertEquals(new Argument<>(ArgumentType.NAMED, "minecraft", "minecraft:air"), arguments[0]);
			assertEquals("minecraft:minecraft:air", arguments[0].raw());
		}

		{
			Argument<String>[] arguments = new FunctionArgumentParser("asgasg:mgjgdfkjhlak:adhgahadh").getArguments();

			assertEquals(1, arguments.length);
			assertEquals(new Argument<>(ArgumentType.NAMED, "asgasg", "mgjgdfkjhlak:adhgahadh"), arguments[0]);
			assertEquals("asgasg:mgjgdfkjhlak:adhgahadh", arguments[0].raw());
		}

		{
			Argument<String>[] arguments = new FunctionArgumentParser("x: y: z").getArguments();

			assertEquals(1, arguments.length);
			assertEquals(new Argument<>(ArgumentType.NAMED, "x", "y: z"), arguments[0]);
			assertEquals("x: y: z", arguments[0].raw());
		}
	}

}
