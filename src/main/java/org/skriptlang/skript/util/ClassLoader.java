package org.skriptlang.skript.util;

import ch.njol.skript.Skript;
import ch.njol.util.StringUtils;
import com.google.common.base.MoreObjects;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * A utility class for loading classes contained in specific packages.
 */
public class ClassLoader {

	/**
	 * @return A builder for creating a loader.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A utility method for loading <b>and initializing</b> all classes within the base package/subpackages.
	 * This method will perform a deep search, meaning classes within subpackages of subpackages will be initialized too.
	 * @param source A class within the resource classes should be loaded from.
	 * @param jarFile A file representing the jar to search for classes.
	 * @param basePackage The package within the jar to load classes from.
	 * @param subPackages The specific subpackages within <code>basePackage</code> to load.
	 *  If not provided, classes within <code>basePackage</code> and all of its subpackages will be loaded.
	 */
	public static void loadClasses(Class<?> source, File jarFile, String basePackage, String... subPackages) {
		builder()
				.basePackage(basePackage)
				.addSubPackages(subPackages)
				.initialize(true)
				.deep(true)
				.build()
				.loadClasses(source, jarFile);
	}

	private final String basePackage;
	private final Collection<String> subPackages;
	private final @Nullable Predicate<String> filter;
	private final boolean initialize;
	private final boolean deep;
	private final @Nullable Consumer<Class<?>> forEachClass;

	private ClassLoader(String basePackage, Collection<String> subPackages, @Nullable Predicate<String> filter,
						boolean initialize, boolean deep, @Nullable Consumer<Class<?>> forEachClass) {
		if (!basePackage.isEmpty()) { // allow empty base package
			basePackage = basePackage.replace('.', '/') + "/";
		}
		this.basePackage = basePackage;
		this.subPackages = subPackages.stream()
				.map(subPackage -> subPackage.replace('.', '/') + "/")
				.collect(Collectors.toSet());
		this.filter = filter;
		this.initialize = initialize;
		this.deep = deep;
		this.forEachClass = forEachClass;
	}

	/**
	 * Loads all classes (from the provided source) meeting the criteria set by this loader.
	 * It is <b>recommended</b> to use one of the methods that also accept a [jar] file
	 *  ({@link #loadClasses(Class, File)} and {@link #loadClasses(Class, JarFile)}) for increased reliability.
	 * @param source A class within the resource classes should be loaded from.
	 */
	public void loadClasses(Class<?> source) {
		loadClasses(source, (JarFile) null);
	}

	/**
	 * Loads all classes (from the provided source) meeting the criteria set by this loader.
	 * @param source A class within the resource classes should be loaded from.
	 * @param jarFile A file representing the jar to search for classes. While it is possible to load the classes without a jar,
	 *  it is recommended to provide one for reliability.
	 * @see #loadClasses(Class, JarFile)                  
	 */
	public void loadClasses(Class<?> source, File jarFile) {
		try (JarFile jar = new JarFile(jarFile)) {
			loadClasses(source, jar);
		} catch (IOException e) {
			// TODO better logging
			Skript.warning("Failed to access jar file: " + e);
			loadClasses(source); // try to load using just the source class
		}
	}

	/**
	 * Loads all classes (from the provided source) meeting the criteria set by this loader.
	 * @param source A class within the resource classes should be loaded from.
	 * @param jar A jar to search for classes. While it is possible to load the classes without this jar,
	 *  it is recommended to provide one for reliability.
	 * @see #loadClasses(Class, File)               
	 */
	public void loadClasses(Class<?> source, @Nullable JarFile jar) {
		final Collection<String> classPaths;
		try {
			if (jar != null) { // load from jar if available
				classPaths = jar.stream()
						.map(JarEntry::getName)
						.collect(Collectors.toSet());
			} else {
				classPaths = ClassPath.from(source.getClassLoader()).getResources().stream()
					.map(ResourceInfo::getResourceName)
					.collect(Collectors.toSet());
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load classes: " + e);
		}

		// Used for tracking valid classes if a non-recursive search is done
		// Depth is the measure of how "deep" from the head package of 'basePackage' a class is
		final int expectedDepth = !this.deep ? StringUtils.count(this.basePackage, '/') : 0;
		final int offset = this.basePackage.length();

		// classes will be loaded in alphabetical order
		Collection<String> classNames = new TreeSet<>(String::compareToIgnoreCase);
		for (String name : classPaths) {
			if (!name.startsWith(this.basePackage) || !name.endsWith(".class") || name.endsWith("package-info.class")) {
				continue;
			}
			boolean load;
			if (this.subPackages.isEmpty()) {
				// loaded only if within base package when deep searches are forbidden
				load = this.deep || StringUtils.count(name, '/') == expectedDepth;
			} else {
				load = false;
				for (String subPackage : this.subPackages) {
					// if the entry is within the subpackage, ensure it is not any deeper if not permitted
					if (name.startsWith(subPackage, offset)
						&& (this.deep || StringUtils.count(name, '/') == expectedDepth + StringUtils.count(subPackage, '/'))) {
						load = true;
						break;
					}
				}
			}

			if (load) {
				// replace separators and .class extension
				name = name.replace('/', '.').substring(0, name.length() - 6);
				if (filter == null || filter.test(name)) { // final check for loading
					classNames.add(name);
				}
			}
		}

		java.lang.ClassLoader loader = source.getClassLoader();
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className, this.initialize, loader);
				if (this.forEachClass != null) {
					this.forEachClass.accept(clazz);
				}
			} catch (ClassNotFoundException ex) {
				throw new RuntimeException("Failed to load class: " + className, ex);
			} catch (ExceptionInInitializerError err) {
				throw new RuntimeException(className + " generated an exception while loading", err.getCause());
			}
		}
	}

	/**
	 * @return A builder representing this ClassLoader.
	 */
	@Contract("-> new")
	public Builder toBuilder() {
		Builder builder = builder()
			.basePackage(this.basePackage)
			.addSubPackages(this.subPackages)
			.initialize(this.initialize)
			.deep(this.deep);
		if (filter != null) {
			builder.filter(this.filter);
		}
		if (forEachClass != null) {
			builder.forEachClass(this.forEachClass);
		}
		return builder;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("basePackage", basePackage)
				.add("subPackages", subPackages)
				.add("filter", filter)
				.add("initialize", initialize)
				.add("deep", deep)
				.add("forEachClass", forEachClass)
				.toString();
	}

	/**
	 * A builder for constructing a {@link ClassLoader}.
	 */
	public static final class Builder {

		private String basePackage = "";
		private final Collection<String> subPackages = new HashSet<>();
		private @Nullable Predicate<String> filter = null;
		private boolean initialize;
		private boolean deep;
		private @Nullable Consumer<Class<?>> forEachClass;

		private Builder() { }

		/**
		 * Sets the package the loader should start loading classes from.
		 * This is <b>required</b>.
		 * @param basePackage A string representing package to start loading classes from.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder basePackage(String basePackage) {
			this.basePackage = basePackage;
			return this;
		}

		/**
		 * Adds a subpackage the loader should start loading classes from.
		 * This is useful for when you may want to load from some, but not all, of the subpackages of the base package.
		 * @param subPackage A string representing a subpackage to load from.
		 * @return This builder.
		 * @see #addSubPackages(String...)
		 * @see #addSubPackages(Collection)
		 */
		@Contract("_ -> this")
		public Builder addSubPackage(String subPackage) {
			this.subPackages.add(subPackage);
			return this;
		}

		/**
		 * Adds subpackages the loader should start loading classes from.
		 * This is useful for when you may want to load from some, but not all, of the subpackages of the base package.
		 * @param subPackages Strings representing subpackages to load from.
		 * @return This builder.
		 * @see #addSubPackage(String)
		 * @see #addSubPackages(Collection)
		 */
		@Contract("_ -> this")
		public Builder addSubPackages(String... subPackages) {
			Collections.addAll(this.subPackages, subPackages);
			return this;
		}

		/**
		 * Adds subpackages the loader should start loading classes from.
		 * This is useful for when you may want to load from some, but not all, of the subpackages of the base package.
		 * @param subPackages Strings representing subpackages to load from.
		 * @return This builder.
		 * @see #addSubPackage(String)
		 * @see #addSubPackages(String...)
		 */
		@Contract("_ -> this")
		public Builder addSubPackages(Collection<String> subPackages) {
			this.subPackages.addAll(subPackages);
			return this;
		}

		/**
		 * A predicate for whether a fully qualified class name should be loaded as a {@link Class}.
		 * @param filter A predicate for filtering class names.
		 *  It should return true for class names to load.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder filter(Predicate<String> filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Sets whether the loader will initialize found classes.
		 * @param initialize Whether classes should be initialized when found.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder initialize(boolean initialize) {
			this.initialize = initialize;
			return this;
		}

		/**
		 * Sets whether the loader will perform a deep search.
		 * @param deep Whether subpackages of the provided base package (or subpackages) should be searched.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder deep(boolean deep) {
			this.deep = deep;
			return this;
		}

		/**
		 * Sets a consumer to be run for each found class.
		 * @param forEachClass A consumer to run for each found class.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		public Builder forEachClass(Consumer<Class<?>> forEachClass) {
			this.forEachClass = forEachClass;
			return this;
		}

		/**
		 * Builds a new loader from the set details.
		 * @return A loader for loading classes through the manner outlined by this builder.
		 */
		@Contract("-> new")
		public ClassLoader build() {
			return new ClassLoader(basePackage, subPackages, filter, initialize, deep, forEachClass);
		}

	}

}
