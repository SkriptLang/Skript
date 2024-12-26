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
		String[][] vs = {
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
		for (String[] v : vs) {
			assert Arrays.equals(Arrays.copyOfRange(v, 1, v.length), FlatFileStorage.splitCSV(v[0])) : v[0] + ": " + Arrays.toString(Arrays.copyOfRange(v, 1, v.length)) + " != " + Arrays.toString(FlatFileStorage.splitCSV(v[0]));
		}
	}

}
