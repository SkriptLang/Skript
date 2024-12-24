package ch.njol.skript.classes;

/**
 * An interface for optionally cloning an object,
 * should return the given object if no cloning is required.
 */
public interface Cloner<T> {
	
	T clone(T t);
	
}
