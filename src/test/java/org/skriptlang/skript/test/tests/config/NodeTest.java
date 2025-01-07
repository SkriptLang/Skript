package org.skriptlang.skript.test.tests.config;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import ch.njol.skript.config.Node;
import ch.njol.util.NonNullPair;

public class NodeTest {

	@Test
	public void splitLineTest() {
		String[][] data = {
				{"", "", ""},
				{"ab", "ab", ""},
				{"ab#", "ab", "#"},
				{"ab##", "ab#", ""},
				{"ab###", "ab#", "#"},
				{"#ab", "", "#ab"},
				{"ab#cd", "ab", "#cd"},
				{"ab##cd", "ab#cd", ""},
				{"ab###cd", "ab#", "#cd"},
				{"######", "", "######"},
				{"#######", "", "#######"},
				{"#### # ####", "", "#### # ####"},
				{"##### ####", "", "##### ####"},
				{"#### #####", "", "#### #####"},
				{"#########", "", "#########"},
				{"a##b#c##d#e", "a#b", "#c##d#e"},
				{" a ## b # c ## d # e ", " a # b ", "# c ## d # e "},
				{"a b \"#a  ##\" # b \"", "a b \"#a  ##\" ", "# b \""},
		};
		for (String[] d : data) {
			NonNullPair<String, String> p = Node.splitLine(d[0]);
			assertArrayEquals(d[0], new String[] {d[1], d[2]}, new String[] {p.getFirst(), p.getSecond()});
		}

	}

}
