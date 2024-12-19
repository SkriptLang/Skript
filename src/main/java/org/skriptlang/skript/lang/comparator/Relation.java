package org.skriptlang.skript.lang.comparator;

/**
 * Represents a relation between two objects.
 */
public enum Relation {

	EQUAL("equal to"),
	NOT_EQUAL("not equal to"),
	GREATER("greater than"),
	GREATER_OR_EQUAL("greater than or equal to"),
	SMALLER("smaller than"),
	SMALLER_OR_EQUAL("smaller than or equal to");

	private final String toString;

	Relation(String toString) {
		this.toString = toString;
	}

	/**
	 * @param b The boolean to get a Relation from.
	 * @return {@link #EQUAL} if 'b' is true else {@link #NOT_EQUAL}
	 */
	public static Relation get(boolean b) {
		return b ? Relation.EQUAL : Relation.NOT_EQUAL;
	}

	/**
	 * @param i The int to get a Relation from.
	 * @return {@link #EQUAL} if 'i' is equal to 0,
	 * {@link #GREATER} if 'i' is greater than 0,
	 * {@link #SMALLER} if 'i' is less than 0
	 */
	public static Relation get(int i) {
		return i == 0 ? Relation.EQUAL : i > 0 ? Relation.GREATER : Relation.SMALLER;
	}

	/**
	 * @param d The double to get a Relation from.
	 * @return {@link #EQUAL} if 'd' is equal to 0,
	 * {@link #GREATER} if 'd' is greater than 0,
	 * {@link #SMALLER} if 'd' is less than 0
	 */
	public static Relation get(double d) {
		return d == 0 ? Relation.EQUAL : d > 0 ? Relation.GREATER : Relation.SMALLER;
	}

	/**
	 * Test whether this Relation is fulfilled if another is, i.e. if the parameter 'other' fulfils <code>X rel Y</code>,
	 * then this Relation fulfils <code>X rel Y</code> as well.
	 *
	 * @param other The Relation to compare with.
	 * @return Whether this Relation is part of the given Relation, e.g. <code>GREATER_OR_EQUAL.isImpliedBy(EQUAL)</code> returns true.
	 */
	public boolean isImpliedBy(Relation other) {
		if (other == this) {
			return true;
		}
		switch (this) {
			case EQUAL:
			case GREATER:
			case SMALLER:
				return false;
			case NOT_EQUAL:
				return other == SMALLER || other == GREATER;
			case GREATER_OR_EQUAL:
				return other == GREATER || other == EQUAL;
			case SMALLER_OR_EQUAL:
				return other == SMALLER || other == EQUAL;
			default:
				throw new IllegalStateException("Unexpected value: " + this);
		}
	}

	/**
	 * @param others The Relations to compare with.
	 * @return True if {@link #isImpliedBy(Relation)} is true for any of the provided Relations.
	 */
	public boolean isImpliedBy(Relation... others) {
		for (Relation other : others) {
			if (isImpliedBy(other)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns this Relation's string representation, which is similar to "equal to" or "greater than".
	 */
	@Override
	public String toString() {
		return toString;
	}

	/**
	 * @return The inverse of this Relation, i.e if this Relation fulfils <code>X rel Y</code>,
	 * then the returned Relation fulfils <code>!(X rel Y)</code>.
	 */
	public Relation getInverse() {
		switch (this) {
			case EQUAL:
				return NOT_EQUAL;
			case NOT_EQUAL:
				return EQUAL;
			case GREATER:
				return SMALLER_OR_EQUAL;
			case GREATER_OR_EQUAL:
				return SMALLER;
			case SMALLER:
				return GREATER_OR_EQUAL;
			case SMALLER_OR_EQUAL:
				return GREATER;
			default:
				throw new IllegalStateException("Unexpected value: " + this);
		}
	}

	/**
	 * @return The Relation which has switched arguments, i.e. if this Relation fulfils <code>X rel Y</code>,
	 * then the returned Relation fulfils <code>Y rel X</code>.
	 */
	public Relation getSwitched() {
		switch (this) {
			case EQUAL:
				return EQUAL;
			case NOT_EQUAL:
				return NOT_EQUAL;
			case GREATER:
				return SMALLER;
			case GREATER_OR_EQUAL:
				return SMALLER_OR_EQUAL;
			case SMALLER:
				return GREATER;
			case SMALLER_OR_EQUAL:
				return GREATER_OR_EQUAL;
			default:
				throw new IllegalStateException("Unexpected value: " + this);
		}
	}

	/**
	 * @return An int relating to the value of this Relation.
	 * <br>0 if {@link #EQUAL} or {@link #NOT_EQUAL}
	 * <br>1 if {@link #GREATER} or {@link #GREATER_OR_EQUAL}
	 * <br>-1 if {@link #SMALLER} or {@link #SMALLER_OR_EQUAL}
	 */
	public int getRelation() {
		switch (this) {
			case EQUAL:
			case NOT_EQUAL:
				return 0;
			case GREATER:
			case GREATER_OR_EQUAL:
				return 1;
			case SMALLER:
			case SMALLER_OR_EQUAL:
				return -1;
			default:
				throw new IllegalStateException("Unexpected value: " + this);
		}
	}

}
