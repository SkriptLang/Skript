package ch.njol.skript.lang;

public interface BuildableObject<T> {

	T getSource();

	Class<? extends T> getReturnType();

}
