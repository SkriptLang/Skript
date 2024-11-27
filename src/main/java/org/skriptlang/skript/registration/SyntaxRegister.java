package org.skriptlang.skript.registration;

import com.google.common.collect.ImmutableSet;

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

	public Collection<I> syntaxes() {
		synchronized (syntaxes) {
			return ImmutableSet.copyOf(syntaxes);
		}
	}

	public void add(I info) {
		syntaxes.add(info);
	}

	public void remove(I info) {
		syntaxes.remove(info);
	}

}
