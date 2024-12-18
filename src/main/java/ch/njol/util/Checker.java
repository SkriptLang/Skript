package ch.njol.util;

@FunctionalInterface
public interface Checker<T> {
	
	public boolean check(T o);
	
}
