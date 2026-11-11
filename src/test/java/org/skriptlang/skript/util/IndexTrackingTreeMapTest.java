package org.skriptlang.skript.util;

import org.junit.Test;

import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings("OverwrittenKey")
public class IndexTrackingTreeMapTest {

	private IndexTrackingTreeMap<String> newMap() {
		return new IndexTrackingTreeMap<>();
	}

	@Test
	public void nextOpenIndexReturnsOneWhenMapIsEmpty() {
		IndexTrackingTreeMap<String> map = newMap();

		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void addUsesKeyOneWhenMapIsEmpty() {
		IndexTrackingTreeMap<String> map = newMap();

		map.add("value");

		assertEquals("value", map.get("1"));
		assertEquals(1, map.size());
		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void nextOpenIndexReturnsOneWhenFirstNumericKeyIsMissing() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("2", "two");
		map.put("3", "three");

		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void nextOpenIndexReturnsNextValueForConsecutiveKeys() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("2", "two");
		map.put("3", "three");

		assertEquals(4, map.nextOpenIndex());
	}

	@Test
	public void nextOpenIndexReturnsFirstGapInMiddle() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("2", "two");
		map.put("4", "four");

		assertEquals(3, map.nextOpenIndex());
	}

	@Test
	public void nextOpenIndexReturnsFirstGapWhenMultipleGapsExist() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("3", "three");
		map.put("5", "five");

		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void nonNumericKeysDoNotAffectNextOpenIndex() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("foo", "foo");
		map.put("bar", "bar");

		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void mixedNumericAndNonNumericKeysOnlyTrackNumericOnes() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("foo", "foo");
		map.put("3", "three");
		map.put("bar", "bar");

		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void overwriteExistingNumericKeyDoesNotDuplicateTracking() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("1", "updated");

		assertEquals(1, map.size());
		assertEquals("updated", map.get("1"));
		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void overwriteExistingNonNumericKeyDoesNotAffectTracking() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("foo", "foo");
		map.put("foo", "updated");

		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void removeExistingNumericKeyReopensThatSlot() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("2", "two");
		map.put("3", "three");

		map.remove("2");

		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void removeFirstNumericKeyReopensOne() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("2", "two");

		map.remove("1");

		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void removeLastNumericKeyDoesNotAffectEarlierGapDetection() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("2", "two");
		map.put("4", "four");

		map.remove("4");

		assertEquals(3, map.nextOpenIndex());
	}

	@Test
	public void removeNonNumericKeyDoesNotAffectTracking() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("foo", "foo");

		map.remove("foo");

		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void removeMissingKeyDoesNothing() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("2", "two");

		map.remove("9");

		assertEquals(3, map.nextOpenIndex());
	}

	@Test
	public void addReusesFirstGap() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("3", "three");

		map.add("two");

		assertEquals("two", map.get("2"));
		assertEquals(4, map.nextOpenIndex());
	}

	@Test
	public void repeatedAddCreatesSequentialNumericKeys() {
		IndexTrackingTreeMap<String> map = newMap();

		map.add("one");
		map.add("two");
		map.add("three");

		assertEquals("one", map.get("1"));
		assertEquals("two", map.get("2"));
		assertEquals("three", map.get("3"));
		assertEquals(4, map.nextOpenIndex());
	}

	@Test
	public void zeroIsIgnoredAsNumericKey() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("0", "zero");

		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void leadingZeroKeyBehaviorIsExplicit() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("01", "leading-zero");

		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void alphaNumericKeyIsIgnored() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1a", "value");
		map.put("a1", "value");

		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void mapIndicesViewIsCorrect() {
		IndexTrackingTreeMap<Object> map = new IndexTrackingTreeMap<>();
		map.put("1", "one");
		Map<String, String> subMap = Map.of("a", "b");
		map.put("sub", subMap);

		Collection<String> indices = map.mapIndices();
		assertEquals(1, indices.size());
		assertTrue(indices.contains("sub"));

		assertThrows(UnsupportedOperationException.class, () -> indices.add("other"));
	}

	@Test
	public void putNullValueRemovesMapping() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("1", null);

		assertNull(map.get("1"));
		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void addMapValueUpdatesMapIndices() {
		IndexTrackingTreeMap<Object> map = new IndexTrackingTreeMap<>();
		Map<String, String> subMap = Map.of("a", "b");
		map.add(subMap);

		assertEquals(subMap, map.get("1"));
		assertTrue(map.mapIndices().contains("1"));
	}

	@Test
	public void removeMapValueUpdatesMapIndices() {
		IndexTrackingTreeMap<Object> map = new IndexTrackingTreeMap<>();
		Map<String, String> subMap = Map.of("a", "b");
		map.put("sub", subMap);
		assertTrue(map.mapIndices().contains("sub"));

		map.remove("sub");
		assertFalse(map.mapIndices().contains("sub"));
	}

	@Test
	public void clearResetsTracking() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("2", "two");

		map.clear();

		//noinspection ConstantValue
		assertTrue(map.isEmpty());
		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void putAllKeepsTrackingCorrect() {
		IndexTrackingTreeMap<String> map = newMap();

		map.putAll(Map.of(
				"1", "one",
				"3", "three",
				"foo", "foo"
		));

		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void largeNonParsableIntegerStringThrows() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("999999999999999999999999", "huge");
		assertEquals(1, map.size());
		assertEquals(1, map.nextOpenIndex());
		assertEquals("huge", map.get("999999999999999999999999"));
	}

}
