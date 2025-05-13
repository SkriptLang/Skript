package ch.njol.skript.registrations;

import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Comparator.Relation;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link org.skriptlang.skript.lang.comparator.Comparators}
 */
@Deprecated
public class Comparators {

	private Comparators() {}

	/**
	 * Registers a {@link Comparator}.
	 *
	 * @param t1
	 * @param t2
	 * @param c
	 * @throws IllegalArgumentException if any given class is equal to <code>Object.class</code>
	 */
	public static <T1, T2> void registerComparator(final Class<T1> t1, final Class<T2> t2, final Comparator<T1, T2> c) {
		org.skriptlang.skript.lang.comparator.Comparators.registerComparator(t1, t2, new org.skriptlang.skript.lang.comparator.Comparator<T1, T2>() {
			@Override
			public org.skriptlang.skript.lang.comparator.Relation compare(T1 o1, T2 o2) {
				return getFromOld(c.compare(o1, o2));
			}

			@Override
			public boolean supportsOrdering() {
				return c.supportsOrdering();
			}
		});
	}

	public static Relation compare(final @Nullable Object o1, final @Nullable Object o2) {
		return getFromNew(org.skriptlang.skript.lang.comparator.Comparators.compare(o1, o2));
	}

	public static java.util.Comparator<Object> getJavaComparator() {
		return (o1, o2) -> compare(o1, o2).getRelation();
	}

	@Nullable
	public static <F, S> Comparator<? super F, ? super S> getComparator(final Class<F> f, final Class<S> s) {
		org.skriptlang.skript.lang.comparator.Comparator<F, S> newComp =
			org.skriptlang.skript.lang.comparator.Comparators.getComparator(f, s);
		if (newComp == null)
			return null;
		return new Comparator<F, S>() {
			@Override
			public Relation compare(F f, S s) {
				return getFromNew(newComp.compare(f, s));
			}

			@Override
			public boolean supportsOrdering() {
				return newComp.supportsOrdering();
			}
		};
	}

	private static Relation getFromNew(org.skriptlang.skript.lang.comparator.Relation newRelation) {
		switch (newRelation) {
			case EQUAL:
				return Relation.EQUAL;
			case NOT_EQUAL:
				return Relation.NOT_EQUAL;
			case SMALLER:
				return Relation.SMALLER;
			case SMALLER_OR_EQUAL:
				return Relation.SMALLER_OR_EQUAL;
			case GREATER:
				return Relation.GREATER;
			case GREATER_OR_EQUAL:
				return Relation.GREATER_OR_EQUAL;
			default:
				throw new IllegalArgumentException("Unexpected value: " + newRelation);
		}
	}

	private static org.skriptlang.skript.lang.comparator.Relation getFromOld(Relation oldRelation) {
		switch (oldRelation) {
			case EQUAL:
				return org.skriptlang.skript.lang.comparator.Relation.EQUAL;
			case NOT_EQUAL:
				return org.skriptlang.skript.lang.comparator.Relation.NOT_EQUAL;
			case SMALLER:
				return org.skriptlang.skript.lang.comparator.Relation.SMALLER;
			case SMALLER_OR_EQUAL:
				return org.skriptlang.skript.lang.comparator.Relation.SMALLER_OR_EQUAL;
			case GREATER:
				return org.skriptlang.skript.lang.comparator.Relation.GREATER;
			case GREATER_OR_EQUAL:
				return org.skriptlang.skript.lang.comparator.Relation.GREATER_OR_EQUAL;
			default:
				throw new IllegalArgumentException("Unexpected value: " + oldRelation);
		}
	}

}
