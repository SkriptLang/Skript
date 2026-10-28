package org.skriptlang.skript.util;

import org.junit.Test;

import java.util.*;

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

	@Test
	public void mapIndicesIsEmptyWithoutSublists() {
		IndexTrackingTreeMap<Object> map = new IndexTrackingTreeMap<>();
		assertTrue(map.mapIndices().isEmpty());

		map.put("1", "one");
		map.remove("sub");
		map.put("sub", null);
		assertTrue(map.mapIndices().isEmpty());
		assertThrows(UnsupportedOperationException.class, () -> map.mapIndices().add("other"));
	}

	@Test
	public void mapIndicesAfterClear() {
		IndexTrackingTreeMap<Object> map = new IndexTrackingTreeMap<>();
		map.put("sub", Map.of("a", "b"));
		map.clear();
		assertTrue(map.mapIndices().isEmpty());

		map.put("other", Map.of("a", "b"));
		assertEquals(Set.of("other"), new HashSet<>(map.mapIndices()));
	}

	@Test
	public void replacingValueWithSublistTracksIt() {
		IndexTrackingTreeMap<Object> map = new IndexTrackingTreeMap<>();
		map.put("b", "value");
		map.put("b", Map.of("y", "sublist"));
		assertEquals(Set.of("b"), new HashSet<>(map.mapIndices()));

		map.put("b", "value again");
		assertTrue(map.mapIndices().isEmpty());
	}

	@Test
	public void onlyLargeIndices() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1761600000", "timestamp");
		map.remove("1761600000");
		map.put("1761600001", "timestamp");

		assertEquals(1, map.nextOpenIndex());
		assertFalse(map.consecutive());
		map.add("one");
		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void removingSmallIndexBeforeAnyTracked() {
		IndexTrackingTreeMap<String> map = newMap();
		map.remove("1");
		map.put("5", null);
		map.put("5", "five");
		map.remove("5");

		assertTrue(map.isEmpty());
		assertEquals(1, map.nextOpenIndex());
	}

	@Test
	public void keysThatOverflowIntAreNotIndices() {
		IndexTrackingTreeMap<String> map = newMap();
		// 4294967297 is 2^32 + 1, which wraps around to 1 if the parse overflows silently
		map.put("4294967297", "wrapped");
		map.put("2147483648", "just past int");

		assertEquals(1, map.nextOpenIndex());
		map.add("one");
		assertEquals("one", map.get("1"));
		assertEquals("wrapped", map.get("4294967297"));
		assertEquals(2, map.nextOpenIndex());
	}

	@Test
	public void largestIntKeyIsHandled() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("2147483647", "max");
		map.add("one");

		assertEquals(2, map.nextOpenIndex());
		map.remove("2147483647");
		assertEquals(2, map.nextOpenIndex());
		map.remove("1");
		assertEquals(1, map.nextOpenIndex());
	}

	// --- growing, backfilling and shrinking the tracked range ---

	@Test
	public void addPastInitialTrackedRange() {
		IndexTrackingTreeMap<String> map = newMap();
		for (int i = 1; i <= 1000; i++) {
			map.add("v" + i);
			assertEquals(i + 1, map.nextOpenIndex());
		}
		for (int i = 1; i <= 1000; i++)
			assertEquals("v" + i, map.get(String.valueOf(i)));
	}

	@Test
	public void keysAboveTrackedRangeArePickedUpWhenGrowing() {
		IndexTrackingTreeMap<String> map = newMap();
		// these are inserted before the tracked range reaches them
		for (int i = 100; i <= 300; i++)
			map.put(String.valueOf(i), "high");

		for (int i = 1; i < 100; i++)
			map.add("low");

		assertEquals(301, map.nextOpenIndex());
		map.add("next");
		assertEquals("next", map.get("301"));
	}

	@Test
	public void gapAboveTrackedRangeIsFoundWhenGrowing() {
		IndexTrackingTreeMap<String> map = newMap();
		for (int i = 65; i <= 500; i++) {
			if (i != 200)
				map.put(String.valueOf(i), "high");
		}

		for (int i = 1; i <= 64; i++)
			map.put(String.valueOf(i), "low");

		assertEquals(200, map.nextOpenIndex());
	}

	@Test
	public void putInRandomOrderEndsConsecutive() {
		IndexTrackingTreeMap<String> map = newMap();
		List<Integer> indices = new ArrayList<>();
		for (int i = 1; i <= 5000; i++)
			indices.add(i);
		Collections.shuffle(indices, new Random(1));

		for (int index : indices)
			map.put(String.valueOf(index), "v");

		assertEquals(5001, map.nextOpenIndex());
		assertTrue(map.consecutive());
	}

	@Test
	public void nullValuedEntriesAreNotIndices() {
		IndexTrackingTreeMap<String> map = newMap();
		for (int i = 1; i <= 64; i++)
			map.add("v");
		map.put("65", null); // TreeMap keeps the key, mapped to null
		map.put("66", "v");

		assertEquals(65, map.nextOpenIndex());
		assertTrue(map.containsKey("65"));
		map.add("filled");
		assertEquals("filled", map.get("65"));
		assertEquals(67, map.nextOpenIndex());
	}

	@Test
	public void nullValuedEntriesAreSkippedWhenGrowing() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("100", "v");
		map.put("101", "v");
		map.put("101", null);

		for (int i = 1; i < 100; i++)
			map.add("v");

		assertEquals(101, map.nextOpenIndex());
	}

	@Test
	public void shrinkingKeepsNextOpenIndex() {
		IndexTrackingTreeMap<String> map = newMap();
		for (int i = 1; i <= 10_000; i++)
			map.add("v");

		for (int i = 10_000; i > 10; i--) {
			map.remove(String.valueOf(i));
			assertEquals(i, map.nextOpenIndex());
		}

		map.add("v");
		assertEquals("v", map.get("11"));
		assertEquals(12, map.nextOpenIndex());
	}

	@Test
	public void keysDroppedByShrinkingArePickedUpAgain() {
		IndexTrackingTreeMap<String> map = newMap();
		for (int i = 1; i <= 1000; i++)
			map.add("v");

		// leaves 1 and 991..1000, which is sparse enough to shrink the tracked range below 991
		for (int i = 2; i <= 990; i++)
			map.remove(String.valueOf(i));
		assertEquals(2, map.nextOpenIndex());

		for (int i = 2; i <= 990; i++)
			map.add("refill");

		assertEquals(1001, map.nextOpenIndex());
		assertEquals(1000, map.size());
		assertTrue(map.consecutive());
	}

	@Test
	public void emptyingAndRefilling() {
		IndexTrackingTreeMap<String> map = newMap();
		for (int round = 0; round < 3; round++) {
			for (int i = 1; i <= 2000; i++)
				map.add("v");
			assertEquals(2001, map.nextOpenIndex());

			for (int i = 1; i <= 2000; i++)
				map.remove(String.valueOf(i));
			assertTrue(map.isEmpty());
			assertEquals(1, map.nextOpenIndex());
		}
	}

	@Test
	public void clearAfterGrowingResetsTracking() {
		IndexTrackingTreeMap<String> map = newMap();
		for (int i = 1; i <= 1000; i++)
			map.add("v");

		map.clear();
		map.put("2", "two");

		assertEquals(1, map.nextOpenIndex());
		map.add("one");
		assertEquals(3, map.nextOpenIndex());
	}

	// --- consecutive() ---

	@Test
	public void consecutiveIsFalseWithoutIndices() {
		IndexTrackingTreeMap<String> map = newMap();
		assertFalse(map.consecutive());

		map.put("foo", "foo");
		assertFalse(map.consecutive());
	}

	@Test
	public void consecutiveIsTrueForIndicesFromOne() {
		IndexTrackingTreeMap<String> map = newMap();
		map.add("one");
		map.add("two");
		map.add("three");
		map.put("foo", "foo");

		assertTrue(map.consecutive());
	}

	@Test
	public void consecutiveIsFalseWithGaps() {
		IndexTrackingTreeMap<String> map = newMap();
		map.put("1", "one");
		map.put("3", "three");
		assertFalse(map.consecutive());

		IndexTrackingTreeMap<String> noOne = newMap();
		noOne.put("2", "two");
		noOne.put("3", "three");
		assertFalse(noOne.consecutive());
	}

	@Test
	public void consecutiveIsFalseWithUntrackedHighIndex() {
		IndexTrackingTreeMap<String> map = newMap();
		map.add("one");
		map.add("two");
		map.put("100000", "far");

		assertFalse(map.consecutive());
	}

	@Test
	public void consecutiveIgnoresNullValuedEntries() {
		IndexTrackingTreeMap<String> map = newMap();
		map.add("one");
		map.add("two");
		map.put("5", "five");
		map.put("5", null);

		assertTrue(map.consecutive());
	}

	// --- worst cases that used to hang ---

	@Test(timeout = 1000)
	public void deletingLargeIndicesIsFast() {
		IndexTrackingTreeMap<String> map = newMap();
		Random random = new Random(1);
		List<String> keys = new ArrayList<>();
		keys.add(String.valueOf(Integer.MAX_VALUE));
		for (int i = 0; i < 1000; i++)
			keys.add(String.valueOf(1 + random.nextInt(Integer.MAX_VALUE)));
		for (String key : keys)
			map.put(key, "v");

		keys.sort(Comparator.<String>comparingInt(Integer::parseInt).reversed());
		for (String key : keys)
			map.remove(key);

		assertTrue(map.isEmpty());
		assertEquals(1, map.nextOpenIndex());
	}

	@Test(timeout = 1000)
	public void deletingTimestampKeysIsFast() {
		IndexTrackingTreeMap<String> map = newMap();
		int now = 1_761_600_000; // seconds since the epoch, October 2025
		for (int i = 0; i < 1000; i++)
			map.put(String.valueOf(now + i), "v");

		for (int i = 999; i >= 0; i--)
			map.remove(String.valueOf(now + i));

		assertTrue(map.isEmpty());
	}

	@Test(timeout = 1000)
	public void removingFirstAndAddingOnLargeListIsFast() {
		IndexTrackingTreeMap<String> map = newMap();
		for (int i = 1; i <= 20_000; i++)
			map.add("v");

		for (int i = 0; i < 20_000; i++) {
			map.remove("1");
			map.add("v");
		}

		assertEquals(20_001, map.nextOpenIndex());
	}

	// --- randomized comparison against a reference model ---

	@Test
	public void matchesReferenceSmallKeys() {
		for (long seed = 0; seed < 10; seed++)
			compareWithReference(seed, 10_000, 150, 1);
	}

	@Test
	public void matchesReferenceAcrossGrowAndShrink() {
		for (long seed = 100; seed < 103; seed++)
			compareWithReference(seed, 60_000, 3000, 50);
	}

	/**
	 * Applies random operations to the map and to a plain {@link TreeMap}, checking after every
	 * {@code checkEvery} operations that the map's tracking matches what the reference map implies.
	 * Operations come in phases that mostly insert or mostly remove, so the tracked range grows and shrinks.
	 *
	 * @param maxIndex the largest small index used for keys
	 */
	private void compareWithReference(long seed, int operations, int maxIndex, int checkEvery) {
		Random random = new Random(seed);
		IndexTrackingTreeMap<Object> map = new IndexTrackingTreeMap<>();
		TreeMap<String, Object> reference = new TreeMap<>();
		Map<String, String> subMap = Map.of("a", "b");

		for (int op = 0; op < operations; op++) {
			// long enough for removals to empty most of the map, so the tracked range shrinks
			boolean inserting = (op / (maxIndex * 4)) % 2 == 0;

			String key = randomKey(random, maxIndex);
			Object value = switch (random.nextInt(20)) {
				case 0 -> null;
				case 1, 2 -> subMap;
				default -> "v" + op;
			};
			String description;

			int choice = random.nextInt(100);
			if (choice < 2) {
				map.clear();
				reference.clear();
				description = "clear";
			} else if (inserting ? choice < 40 : choice < 85) {
				map.remove(key);
				reference.remove(key);
				description = "remove " + key;
			} else if (choice < (inserting ? 70 : 92) && value != null) {
				int index = map.nextOpenIndex();
				map.add(value);
				reference.put(String.valueOf(index), value);
				description = "add (at " + index + ")";
			} else {
				map.put(key, value);
				reference.put(key, value);
				description = "put " + key + " = " + value;
			}

			if (op % checkEvery == 0 || op == operations - 1)
				assertMatchesReference(map, reference, "seed " + seed + ", op " + op + ": " + description);
		}
	}

	private String randomKey(Random random, int maxIndex) {
		return switch (random.nextInt(20)) {
			case 0 -> String.valueOf(1 + random.nextInt(Integer.MAX_VALUE)); // large index
			case 1 -> random.nextBoolean() ? "4294967297" : "2147483647"; // past int / largest int
			case 2 -> "key" + random.nextInt(10);
			case 3 -> random.nextBoolean() ? "0" : "0" + (1 + random.nextInt(9)); // not indices
			default -> String.valueOf(1 + random.nextInt(maxIndex));
		};
	}

	private void assertMatchesReference(IndexTrackingTreeMap<Object> map, TreeMap<String, Object> reference, String context) {
		assertEquals(context, reference, map);

		int expectedNext = 1;
		while (reference.get(String.valueOf(expectedNext)) != null)
			expectedNext++;
		assertEquals(context, expectedNext, map.nextOpenIndex());

		boolean expectedConsecutive = expectedNext > 1;
		Set<String> expectedMapIndices = new HashSet<>();
		for (Map.Entry<String, Object> entry : reference.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map)
				expectedMapIndices.add(entry.getKey());
			if (value != null && isIndexAbove(entry.getKey(), expectedNext))
				expectedConsecutive = false;
		}
		assertEquals(context, expectedConsecutive, map.consecutive());
		assertEquals(context, expectedMapIndices, new HashSet<>(map.mapIndices()));
	}

	private static boolean isIndexAbove(String key, int bound) {
		if (!key.matches("[1-9][0-9]{0,9}"))
			return false;
		long index = Long.parseLong(key);
		return index <= Integer.MAX_VALUE && index > bound;
	}

}
