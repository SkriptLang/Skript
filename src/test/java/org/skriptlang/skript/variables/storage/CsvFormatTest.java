package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import org.junit.Test;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * Tests {@link CsvFormat}, the format of CSV variable files.
 */
public class CsvFormatTest {

	@Test
	public void hexRoundTrips() {
		byte[] bytes = {-0x80, -0x50, -0x01, 0x00, 0x01, 0x44, 0x7F};
		String string = "80B0FF0001447F";
		assertEquals(string, CsvFormat.encode(bytes));
		assertArrayEquals(bytes, CsvFormat.decode(string));
		assertArrayEquals(bytes, CsvFormat.decode(string.toLowerCase()));
		assertArrayEquals(new byte[0], CsvFormat.decode(""));
	}

	@Test
	public void malformedHexIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> CsvFormat.decode("F"));
		assertThrows(IllegalArgumentException.class, () -> CsvFormat.decode("GG"));
		assertThrows(IllegalArgumentException.class, () -> CsvFormat.decode("0 "));
		assertThrows(IllegalArgumentException.class, () -> CsvFormat.decode("００")); // fullwidth digits
	}

	@Test
	public void linesSplitIntoTheirValues() {
		String[][] cases = {
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
		};
		for (String[] test : cases)
			assertArrayEquals(test[0], Arrays.copyOfRange(test, 1, test.length), CsvFormat.splitCSV(test[0]));
	}

	@Test
	public void invalidLinesDontSplit() {
		assertNull(CsvFormat.splitCSV("a, \"unclosed"));
		assertNull(CsvFormat.splitCSV("a, b\"c\""));
	}

	@Test
	public void escapedValuesSplitWithTheirEscapesUndone() {
		String[][] cases = {
			{"E\"a\\nb\"", "a\nb"},
			{"E\"a\\rb\"", "a\rb"},
			{"E\"\\r\\n\"", "\r\n"},
			{"E\"back\\\\slash\\n\"", "back\\slash\n"},
			{"E\"literal \\\\n, real \\n\"", "literal \\n, real \n"},
			{"E\"say \"\"hi\"\"\\n\"", "say \"hi\"\n"},
			{"E\"\"", ""},
			{" E\"a\\nb\" , string, 01", "a\nb", "string", "01"},
			{"x, E\"y\\n\", z", "x", "y\n", "z"},
			// only the escaped form has escapes, so older quoted values read as they always did
			{"\"a\\nb\"", "a\\nb"},
			// an unquoted E is just an E
			{"E, x", "E", "x"},
		};
		for (String[] test : cases)
			assertArrayEquals(test[0], Arrays.copyOfRange(test, 1, test.length), CsvFormat.splitCSV(test[0]));
	}

	@Test
	public void invalidEscapesDontSplit() {
		assertNull(CsvFormat.splitCSV("E\"a\\tb\"")); // not one of the escapes
		assertNull(CsvFormat.splitCSV("E\"a\\\"")); // a backslash with nothing after it
		assertNull(CsvFormat.splitCSV("E \"a\\nb\"")); // the E has to be right before the quote
		assertNull(CsvFormat.splitCSV("e\"a\\nb\"")); // and uppercase
		assertNull(CsvFormat.splitCSV("E\"unclosed\\n"));
	}

	@Test
	public void onlyValuesWithLineBreaksAreEscaped() {
		StringBuilder text = new StringBuilder();
		CsvFormat.appendRow(text, "back\\slash and \"quotes\"", null);
		assertEquals("\"back\\slash and \"\"quotes\"\"\", null, \n", text.toString());
		text.setLength(0);
		CsvFormat.appendRow(text, "a\nb", null);
		assertEquals("E\"a\\nb\", null, \n", text.toString());
	}

	@Test
	public void writtenRowsSplitBackIntoTheSameValues() {
		String[] names = {"plain", "with, comma", "with \"quotes\"", "with # hash", "with space", "ünï", "list::*",
			"back\\slash", "line\nbreak", "carriage\rreturn", "crlf\r\n", "literal \\n and a real\n", "quote\" and\nbreak",
			"E\"looks escaped", "E\"looks\\nescaped\n", "list\nname::*"};
		for (String name : names) {
			StringBuilder text = new StringBuilder();
			CsvFormat.appendRow(text, name, new SerializedVariable.Value("string", new byte[] {1, 2, 3}));
			CsvFormat.appendRow(text, name, null);
			String[] lines = text.toString().split("\n", -1);
			assertEquals(name, 3, lines.length); // two rows, each ending with a line break
			assertArrayEquals(name, new String[] {name, "string", "010203"}, CsvFormat.splitCSV(lines[0]));
			assertArrayEquals(name, new String[] {name, CsvFormat.DELETED, ""}, CsvFormat.splitCSV(lines[1]));
		}
	}

	@Test
	public void listNames() {
		assertTrue(CsvFormat.isListName("a::*"));
		assertTrue(CsvFormat.isListName("a::b::*"));
		assertFalse(CsvFormat.isListName("a*"));
		assertFalse(CsvFormat.isListName("a::b"));
		assertEquals("a::b::", CsvFormat.listPrefix("a::b::*"));
	}

	@Test
	public void aListsRangeHoldsExactlyItsElements() {
		TreeMap<String, Integer> map = new TreeMap<>();
		String[] inside = {"x::", "x::a", "x::yz", "x::a::b", "x::￿z", "x::😀"};
		String[] outside = {"x", "x:;", "x:a", "xa", "y::a", "w::a"};
		for (String key : inside)
			map.put(key, 1);
		for (String key : outside)
			map.put(key, 1);

		String prefix = CsvFormat.listPrefix("x::*");
		assertEquals("x:;", CsvFormat.prefixEnd(prefix));
		assertEquals(new TreeSet<>(Arrays.asList(inside)), map.subMap(prefix, CsvFormat.prefixEnd(prefix)).keySet());
	}

}
