package org.skriptlang.skript.test.tests.utils;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.junit.Assert;
import org.junit.Test;

import static ch.njol.skript.SkriptCommand.parseAsCommaSeparatedList;

public class CommaArgumentsTest extends SkriptJUnitTest {

	private String[] getArgs(boolean reverse, boolean excludeEmptyArgs, String ... strings) {
		return parseAsCommaSeparatedList(reverse, excludeEmptyArgs, strings).toArray(String[]::new);
	}

	@Test
	public void test() {
		Assert.assertArrayEquals(
			getArgs(false, true,
				"left.sk,",
				"", ",",
				"right.sk,",
				"", ",",
				"left", "right.sk"
			),
			new String[] {"left.sk", "right.sk", "left right.sk"}
		);
		Assert.assertArrayEquals(
			getArgs(false, false,
				"left.sk,",
				"", ",",
				"right.sk,",
				"", ",",
				"left", "right.sk"
			),
			new String[] {"left.sk", "", "right.sk", "", "left right.sk"}
		);

		Assert.assertArrayEquals(
			getArgs(true, true,
				"left.sk,",
				"", ",",
				"right.sk,",
				"", ",",
				"left", "right.sk"
			),
			new String[] {"left right.sk", "right.sk", "left.sk"}
		);
		Assert.assertArrayEquals(
			getArgs(true, false,
				"left.sk,",
				"", ",",
				"right.sk,",
				"", ",",
				"left", "right.sk,",
				""
			),
			new String[] {"", "left right.sk", "", "right.sk", "", "left.sk"}
		);
	}

}
