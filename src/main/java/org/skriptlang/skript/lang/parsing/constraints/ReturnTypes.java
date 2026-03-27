package org.skriptlang.skript.lang.parsing.constraints;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * Set-like object for storing return types.
 * If Object.class is added, the set will contain only an Object classinfo.
 */
public class ReturnTypes implements Iterable<ClassInfoReference> {

	private final HashSet<ClassInfoReference> backingSet = new HashSet<>();
	private boolean isObject = false;

	public boolean add(ClassInfoReference... infoReferences) {
		if (isObject)
			return true;
		boolean success = true;
		for (ClassInfoReference ref : infoReferences) {
			if (ref.getClassInfo().getC() == Object.class) {
				backingSet.clear();
				isObject = true;
				backingSet.add(ref);
				return true;
			}
			success &= backingSet.add(ref);
		}
		return success;
	}

	public boolean add(ClassInfo<?>... infos) {
		if (isObject)
			return true;
		return add(Arrays.stream(infos)
			.map(info -> new ClassInfoReference(info, Kleenean.UNKNOWN))
			.toArray(ClassInfoReference[]::new));
	}

	public boolean add(Class<?>... types) {
		if (isObject)
			return true;
		return add(Arrays.stream(types)
			.map(Classes::getSuperClassInfo)
			.map(info -> new ClassInfoReference(info, Kleenean.UNKNOWN))
			.toArray(ClassInfoReference[]::new));
	}

	public boolean contains(ClassInfoReference ref) {
		return backingSet.contains(ref);
	}

	public boolean contains(ClassInfo<?> info) {
		for (ClassInfoReference ref : backingSet) {
			if (ref.getClassInfo().equals(info))
				return true;
		}
		return false;
	}

	public boolean contains(Class<?> type) {
		for (ClassInfoReference ref : backingSet) {
			if (ref.getClassInfo().getC() == type)
				return true;
		}
		return false;
	}

	public @UnmodifiableView Set<ClassInfoReference> asSet() {
		return Collections.unmodifiableSet(backingSet);
	}

	public boolean isValidReturnType(Class<?> returnType) {
		if (isObject)
			return true;
		for (ClassInfoReference ref : backingSet) {
			if (ref.getClassInfo().getC().isAssignableFrom(returnType))
				return true;
		}
		return false;
	}

	public int size() {
		return backingSet.size();
	}

	@Override
	public @NotNull Iterator<ClassInfoReference> iterator() {
		return backingSet.iterator();
	}



}
