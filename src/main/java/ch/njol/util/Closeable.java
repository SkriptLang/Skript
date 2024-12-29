package ch.njol.util;

/**
 * Like {@link java.io.Closeable}, but not used for resources, thus it neither throws checked exceptions nor causes resource leak warnings.
 * 
 * @author Peter Güttinger
 */
public interface Closeable {
	
	/**
	 * Closes this object. This method may be called multiple times and may or may not have an effect on subsequent calls (e.g. a task might be stopped, but resumed later and
	 * stopped again).
	 */
	public void close();
	
}
