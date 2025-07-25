package ch.njol.skript.lang;

public interface BuildableObject<E> {

	E getSource();

	Class<? extends E> getReturnType();

}
