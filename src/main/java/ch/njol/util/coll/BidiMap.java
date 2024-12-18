package ch.njol.util.coll;

import java.util.Map;
import java.util.Set;

/**
 * @author Peter Güttinger
 */
public interface BidiMap<T1, T2> extends Map<T1, T2> {
	
	public BidiMap<T2, T1> getReverseView();
	
	public T1 getKey(final T2 value);
	
	public T2 getValue(final T1 key);
	
	public Set<T2> valueSet();
	
}
