package ch.njol.util;

/**
 * Like {@link java.io.Closeable}, but not used for resources, thus it neither throws checked exceptions nor causes resource leak warnings.
 *
 * This is an auto-closeable resource and so may be used within a try-with-resources section for automatic disposal.
 * @deprecated use {@link java.io.Closeable}
 */
@Deprecated(forRemoval = true)
public interface Closeable extends AutoCloseable {

	/**
	 * Closes this object. This method may be called multiple times and may or may not have an effect on subsequent calls (e.g. a task might be stopped, but resumed later and
	 * stopped again).
	 */
	void close();

}
