package ch.njol.skript.classes;

/**
 * @author Peter Güttinger
 * @deprecated This class is no longer exposed in newer versions. It should not be used or referenced.
 */
@Deprecated
public class InverseComparator<T1, T2> implements Comparator<T1, T2> {

	private final Comparator<? super T2, ? super T1> comp;

	public InverseComparator(final Comparator<? super T2, ? super T1> c) {
		comp = c;
	}

	@Override
	public Relation compare(final T1 o1, final T2 o2) {
		return comp.compare(o2, o1).getSwitched();
	}

	@Override
	public boolean supportsOrdering() {
		return comp.supportsOrdering();
	}

	@Override
	public String toString() {
		return "InverseComparator(" + comp + ")";
	}

}
