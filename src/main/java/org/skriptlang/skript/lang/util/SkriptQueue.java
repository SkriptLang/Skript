package org.skriptlang.skript.lang.util;

import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A queue of elements.
 * Elements will only be added to the queue if they are not null, with nothing happening if the elements are null.
 */
public class SkriptQueue extends LinkedList<@NotNull Object>
	implements Deque<Object>, Queue<Object>, YggdrasilSerializable, AnyAmount {

	@Override
	public boolean add(Object element) {
		if (element == null)
			return false;
		return super.add(element);
	}

	@Override
	public void add(int index, Object element) {
		if (element != null)
			super.add(index, element);
	}

	@Override
	public void addFirst(Object element) {
		if (element != null)
			super.addFirst(element);
	}

	@Override
	public void addLast(Object element) {
		if (element != null)
			super.addLast(element);
	}

	@Override
	public boolean contains(Object o) {
		if (o == null)
			return false;
		return super.contains(o);
	}

	@Override
	public Object set(int index, Object element) {
		if (element == null)
			return null;
		return super.set(index, element);
	}

	@Override
	public boolean addAll(int index, Collection<?> list) {
		List<?> copy = new ArrayList<>(list);
		copy.removeIf(Objects::isNull);
		return super.addAll(index, copy);
	}

	@Override
	public @NotNull Object @NotNull [] toArray() {
		return super.toArray();
	}

	public Object removeSafely(int i) {
		if (i >= 0 && i < this.size())
			return this.remove(i);
		return null;
	}

	/**
	 * Removes a range of elements from the queue.
	 * This will adjust the start/end of the range to be within the bounds of the queue,
	 * to avoid throwing an error.
	 *
	 * @param fromIndex The starting index (inclusive)
	 * @param toIndex The ending index (exclusive)
	 * @return The removed entries
	 */
	public Object[] removeRangeSafely(int fromIndex, int toIndex) {
		int from, to;
		from = Math.min(this.size(), Math.min(fromIndex, toIndex));
		to = Math.max(0, Math.max(fromIndex, toIndex));
		ListIterator<Object> it = this.listIterator(from);
		Object[] elements = new Object[to - from];
		for (int i = 0, n = to - from; i < n; i++) {
			elements[i] = it.next();
			it.remove();
		}
		return elements;
	}

	@Override
	public @NotNull Number amount() {
		return this.size();
	}

}
