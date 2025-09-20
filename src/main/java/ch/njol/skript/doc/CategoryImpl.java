package ch.njol.skript.doc;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.AddonModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class CategoryImpl implements Category {

	private static final Set<Category> instances = new HashSet<>();
	private final String name;
	private final Category parent;
	private final Set<String> keywords;
	private final Set<Class<? extends AddonModule>> modules;

	public static Set<Category> getInstances() {
		return instances;
	}

	CategoryImpl(String name, String... keywords) {
		this(name, (Category) null);
	}

	CategoryImpl(String name, Category parent, String... keywords) {
		instances.add(this);
		this.name = name;
		this.parent = parent;
		this.keywords = Arrays.stream(keywords)
				.map(String::toLowerCase)
				.collect(Collectors.toUnmodifiableSet());
		this.modules = new HashSet<>();
	}

	@Override
	public @NotNull String name() {
		return name;
	}

	@Override
	public Category parent() {
		return parent;
	}

	public @NotNull Set<String> keywords() {
		return keywords;
	}

	@Override
	public void addModule(@NotNull Class<? extends AddonModule> module) {
		Preconditions.checkNotNull(module, "module cannot be null");

		modules.add(module);
	}

	@Override
	public @Unmodifiable @NotNull Set<Class<? extends AddonModule>> modules() {
		return Set.copyOf(modules);
	}

}
