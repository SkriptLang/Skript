package org.skriptlang.skript.test.tests.localization;

import ch.njol.skript.localization.Noun;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NounTest {

	@Test
	public void testGetPlural() {
		String[][] tests = {
				{"a", "a", "a"},
				{"a¦b", "a", "ab"},
				{"a¦b¦c", "ab", "ac"},
				{"a¦b¦c¦d", "abd", "acd"},
				{"a¦b¦c¦d¦e", "abd", "acde"},
				{"a¦b¦c¦d¦e¦f", "abde", "acdf"},
				{"a¦b¦c¦d¦e¦f¦g", "abdeg", "acdfg"},
		};
		for (String[] test : tests) {
			Noun.PluralPair p = Noun.parsePlural(test[0]);
			assertEquals(test[1], p.singular());
			assertEquals(test[2], p.plural());
		}
	}

	@Test
	public void testNormalizePluralMarkers() {
		String[][] tests = {
				{"a", "a"},
				{"a¦b", "a¦¦b¦"},
				{"a¦b¦c", "a¦b¦c¦"},
				{"a¦b¦c¦d", "a¦b¦c¦d"},
				{"a¦b¦c¦d¦e", "a¦b¦c¦d¦¦e¦"},
				{"a¦b¦c¦d¦e¦f", "a¦b¦c¦d¦e¦f¦"},
				{"a¦b¦c¦d¦e¦f¦g", "a¦b¦c¦d¦e¦f¦g"},
		};
		for (String[] test : tests) {
			assertEquals(test[1], Noun.normalizePluralMarkers(test[0]));
			assertEquals(test[1] + "@x", Noun.normalizePluralMarkers(test[0] + "@x"));
		}
	}

}
