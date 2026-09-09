package ch.njol.skript.variables;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class FlatFileStorageTest {

	@Test
	public void testHexCoding() {
		byte[] bytes = {-0x80, -0x50, -0x01, 0x00, 0x01, 0x44, 0x7F};
		String string = "80B0FF0001447F";
		assertEquals(string, FlatFileStorage.encode(bytes));
		assert Arrays.equals(bytes, FlatFileStorage.decode(string)) : Arrays.toString(bytes) + " != " + Arrays.toString(FlatFileStorage.decode(string));
	}

	@Test
	public void testSplitCSV() {
		// [0] = input
		// [1..] = expected
		String[][] variables = {
			{"", ""},
			{",", "", ""},
			{",,", "", "", ""},
			{"a", "a"},
			{"a,", "a", ""},
			{",a", "", "a"},
			{",a,", "", "a", ""},
			{" , a , ", "", "a", ""},
			{"a,b,c", "a", "b", "c"},
			{" a , b , c ", "a", "b", "c"},

			{"\"\"", ""},
			{"\",\"", ","},
			{"\"\"\"\"", "\""},
			{"\" \"", " "},
			{"a, \"\"\"\", b, \", c\", d", "a", "\"", "b", ", c", "d"},
			{"a, \"\"\", b, \", c", "a", "\", b, ", "c"},

			{"\"\t\0\"", "\t\0"},
			{"\"a\"", "a"},
		};
		for (String[] variable : variables) {
			assert Arrays.equals(Arrays.copyOfRange(variable, 1, variable.length), FlatFileStorage.splitCSV(variable[0])) : variable[0] + ": " + Arrays.toString(Arrays.copyOfRange(variable, 1, variable.length)) + " != " + Arrays.toString(FlatFileStorage.splitCSV(variable[0]));
		}
	}

	@Test
	public void testSplitCSVInvalidInputs() {
		String[] invalidInputs = {
			"a\"b,c",          // random quote inside an unquoted value
			"\"abc",           // unterminated quoted value
			"\"abc\"def,ghi",  // random string after a closing quote
			"a,\"b\"\"c",      // unterminated quote in the last string
			"\"a\" \"b\"",     // two quoted strings with no comma between
		};

		for (String input : invalidInputs) {
			String[] result = FlatFileStorage.splitCSV(input);
			assert result == null : input + ": expected null but got " + Arrays.toString(result);
		}
	}

}
