package ch.njol.skript.doc;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.util.Priority;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class CategoryImpl implements Category {

	private static final Set<Category> instances = new HashSet<>();
	private final String name;
	private final int priority;
	private final Set<String> keywords;
	private final Set<AddonModule> modules;

	public static Set<Category> getInstances() {
		return instances;
	}

	CategoryImpl(String name, int priority, Set<String> keywords) {
		instances.add(this);
		this.name = name;
		this.priority = priority;
		this.keywords = keywords.stream().map(String::toLowerCase).collect(Collectors.toSet());
		this.modules = new HashSet<>();
	}

	@Override
	public @NotNull String name() {
		return name;
	}

	@Override
	public int priority() {
		return priority;
	}

	@Override
	public @NotNull Set<String> keywords() {
		return keywords;
	}

	@Override
	public @NotNull Set<AddonModule> modules() {
		return modules;
	}

}
