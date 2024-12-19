package ch.njol.yggdrasil;

import org.jetbrains.annotations.Nullable;

public interface ClassResolver {
	
	/**
	 * Resolves a class by its ID.
	 * 
	 * @param id The ID used when storing objects
	 * @return The Class object that represents data with the given ID, or null if the ID does not belong to the implementor
	 */
	@Nullable
	Class<?> getClass(String id);
	
	/**
	 * Gets an ID for a Class. The ID is used to identify the type of saved object.
	 * <p>
	 * @param clazz The class to get the ID of
	 * @return The ID of the given class, or null if this is not a class of the implementor
	 */
	// TODO make sure that it's unique
	@Nullable
	String getID(Class<?> clazz);
	
}
