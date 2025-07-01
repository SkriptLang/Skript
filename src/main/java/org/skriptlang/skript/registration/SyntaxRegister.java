package org.skriptlang.skript.registration;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A syntax register is a collection of registered {@link SyntaxInfo}s of a common type.
 * @param <I> The type of syntax in this register.
 */
final class SyntaxRegister<I extends SyntaxInfo<?>> {

	private static final Comparator<SyntaxInfo<?>> SET_COMPARATOR = (a,b) -> {
		if (a == b) { // only considered equal if registering the same infos
			return 0;
		}
		int result = a.priority().compareTo(b.priority());
		// when elements have the same priority, the oldest element comes first
		return result != 0 ? result : 1;
	};

	final Set<I> syntaxes = new ConcurrentSkipListSet<>(SET_COMPARATOR);
	private volatile @Nullable Set<I> cache = null;

	public Collection<I> syntaxes() {
		if (cache == null) {
			cache = ImmutableSet.copyOf(syntaxes);
		}
		return cache;
	}

	public void add(I info) {
		syntaxes.add(info);
		cache = null;
	}

	public void remove(I info) {
		syntaxes.remove(info);
		cache = null;
	}

}
