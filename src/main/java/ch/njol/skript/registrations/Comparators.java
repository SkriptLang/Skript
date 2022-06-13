/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.registrations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Comparator.ComparatorInfo;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.InverseComparator;
import ch.njol.util.Pair;

/**
 * @author Peter Güttinger
 */
public class Comparators {
	
	private Comparators() {}
	
	public final static Collection<ComparatorInfo<?, ?>> comparators = new ArrayList<>();
	
	/**
	 * Registers a {@link Comparator}.
	 * 
	 * @param t1
	 * @param t2
	 * @param c
	 * @throws IllegalArgumentException if any given class is equal to <code>Object.class</code>
	 */
	public static <T1, T2> void registerComparator(final Class<T1> t1, final Class<T2> t2, final Comparator<T1, T2> c) {
		Skript.checkAcceptRegistrations();
		if (t1 == Object.class && t2 == Object.class)
			throw new IllegalArgumentException("You must not add a comparator for Objects");
		comparators.add(new ComparatorInfo<>(t1, t2, c));
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Relation compare(final @Nullable Object o1, final @Nullable Object o2) {
		if (o1 == null || o2 == null)
			return Relation.NOT_EQUAL;
		final Comparator c = getComparator(o1.getClass(), o2.getClass());
		
		if (c == null)
			return Relation.NOT_EQUAL;
		return c.compare(o1, o2);
	}
	
	private final static java.util.Comparator<Object> javaComparator = new java.util.Comparator<Object>() {
		@Override
		public int compare(final @Nullable Object o1, final @Nullable Object o2) {
			return Comparators.compare(o1, o2).getRelation();
		}
	};
	
	public static java.util.Comparator<Object> getJavaComparator() {
		return javaComparator;
	}
	
	private final static Map<Pair<Class<?>, Class<?>>, Comparator<?, ?>> comparatorsQuickAccess = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <F, S> Comparator<? super F, ? super S> getComparator(final Class<F> f, final Class<S> s) {
		final Pair<Class<?>, Class<?>> p = new Pair<>(f, s);
		if (comparatorsQuickAccess.containsKey(p))
			return (Comparator<? super F, ? super S>) comparatorsQuickAccess.get(p);
		final Comparator<?, ?> comp = getComparator_i(f, s);
		comparatorsQuickAccess.put(p, comp);
		return (Comparator<? super F, ? super S>) comp;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	private static <F, S> Comparator<?, ?> getComparator_i(final Class<F> f, final Class<S> s) {
		
		// perfect match
		for (final ComparatorInfo<?, ?> info : comparators) {
			if (info.c1.isAssignableFrom(f) && info.c2.isAssignableFrom(s)) {
				return info.c;
			} else if (info.c1.isAssignableFrom(s) && info.c2.isAssignableFrom(f)) {
				return new InverseComparator<F, S>((Comparator<? super S, ? super F>) info.c);
			}
		}
		
		// same class but no comparator
		if (s == f && f != Object.class && s != Object.class) {
			return Comparator.equalsComparator;
		}
		
		final boolean[] trueFalse = {true, false};
		Converter<? super F, ?> c1;
		Converter<? super S, ?> c2;
		
		// single conversion
		for (final ComparatorInfo<?, ?> info : comparators) {
			for (final boolean first : trueFalse) {
				if (info.getType(first).isAssignableFrom(f)) {
					c2 = Converters.getConverter(s, info.getType(!first));
					if (c2 != null) {
						return first ? new ConvertedComparator<F, S>(info.c, c2) : new InverseComparator<>(new ConvertedComparator<S, F>(c2, info.c));
					}
				}
				if (info.getType(first).isAssignableFrom(s)) {
					c1 = Converters.getConverter(f, info.getType(!first));
					if (c1 != null) {
						return !first ? new ConvertedComparator<F, S>(c1, info.c) : new InverseComparator<>(new ConvertedComparator<S, F>(info.c, c1));
					}
				}
			}
		}
		
		// double conversion
		for (final ComparatorInfo<?, ?> info : comparators) {
			for (final boolean first : trueFalse) {
				c1 = Converters.getConverter(f, info.getType(first));
				c2 = Converters.getConverter(s, info.getType(!first));
				if (c1 != null && c2 != null) {
					return first ? new ConvertedComparator<F, S>(c1, info.c, c2) : new InverseComparator<>(new ConvertedComparator<S, F>(c2, info.c, c1));
				}
			}
		}
		
		return null;
	}
	
	private final static class ConvertedComparator<T1, T2> implements Comparator<T1, T2> {
		
		@SuppressWarnings("rawtypes")
		private final Comparator c;
		@SuppressWarnings("rawtypes")
		@Nullable
		private final Converter c1, c2;
		
		public ConvertedComparator(final Converter<? super T1, ?> c1, final Comparator<?, ?> c) {
			this.c1 = c1;
			this.c = c;
			this.c2 = null;
		}
		
		public ConvertedComparator(final Comparator<?, ?> c, final Converter<? super T2, ?> c2) {
			this.c1 = null;
			this.c = c;
			this.c2 = c2;
		}
		
		public ConvertedComparator(final Converter<? super T1, ?> c1, final Comparator<?, ?> c, final Converter<? super T2, ?> c2) {
			this.c1 = c1;
			this.c = c;
			this.c2 = c2;
		}
		
		@SuppressWarnings({"rawtypes", "unchecked"})
		@Override
		public Relation compare(final T1 o1, final T2 o2) {
			final Converter c1 = this.c1;
			final Object t1 = c1 == null ? o1 : c1.convert(o1);
			if (t1 == null)
				return Relation.NOT_EQUAL;
			final Converter c2 = this.c2;
			final Object t2 = c2 == null ? o2 : c2.convert(o2);
			if (t2 == null)
				return Relation.NOT_EQUAL;
			return c.compare(t1, t2);
		}
		
		@Override
		public boolean supportsOrdering() {
			return c.supportsOrdering();
		}
		
		@Override
		public String toString() {
			return "ConvertedComparator(" + c1 + "," + c + "," + c2 + ")";
		}
		
	}
	
}
