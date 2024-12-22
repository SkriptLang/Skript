/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.config.validate.SectionValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Validated;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * A reference to a single config shared between multiple resources.
 * This is used to handle the (re)loading across all things.
 */
public class SharedConfig extends Config implements Validated, NodeNavigator {

	private final ConfigRegistry manager;
	private final File file;

	@SuppressWarnings("NotNullFieldNotInitialized") // It technically is initialised during constructor
	private @NotNull Config config;

	public SharedConfig(ConfigRegistry manager, String fileName, File file) {
		super(fileName, file);
		this.file = file;
		this.manager = manager;
		this.load();
	}

	public SharedConfig(ConfigRegistry manager, Config initial) {
		super(initial.getFileName(), initial.getFile());
		this.file = Objects.requireNonNull(initial.getFile());
		this.manager = manager;
		this.config = initial;
	}

	private @NotNull Config fallbackConfig() {
		return new Config(fileName, file); // always use an empty dummy to prevent NPEs
	}

	public synchronized void load() {
		try {
			this.config = manager.loadConfig(file);
		} catch (IOException e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "An error occurred while loading config '" + this.fileName + "'");
			this.config = this.fallbackConfig();
		}
	}

	public void unload() {
		this.config = this.fallbackConfig(); // we just need it to be garbage-collected
		this.config.invalidate(); // if something tries to call this, we need to load it again
	}

	public void reload() {
		this.unload(); // in case of other implementations or autosave on unload
		this.load();
	}

	public synchronized void save() {
		try {
			this.save(file);
		} catch (IOException e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "An error occurred while saving config '" + file.getName() + "'");
		}
	}

	@Override
	public void invalidate() throws UnsupportedOperationException {
	}

	/**
	 * A shared config is never invalid, since it can reload upon becoming invalid.
	 *
	 * @return true
	 */
	@Override
	public boolean valid() {
		return true;
	}

	/**
	 * Makes sure this is ready to be navigated.
	 */
	public void ensureReady() {
		if (config.valid())
			return;
		// was probably unloaded by something else & then re-used
		this.load();
	}

	@Override
	public @NotNull File getFile() {
		return file;
	}

	@Override
	public @Nullable Node get(String key) {
		this.ensureReady();
		return config.get(key);
	}

	@Override
	public @NotNull Node getCurrentNode() {
		this.ensureReady();
		return config.getCurrentNode();
	}

	@Override
	void setIndentation(String indent) {
		this.config.setIndentation(indent);
	}

	@Override
	String getIndentation() {
		return config.getIndentation();
	}

	@Override
	String getIndentationName() {
		return config.getIndentationName();
	}

	@Override
	public SectionNode getMainNode() {
		this.ensureReady();
		return config.getMainNode();
	}

	@Override
	public void save(File file) throws IOException {
		this.config.save(file);
	}

	@Override
	@Deprecated(forRemoval = true)
	public boolean setValues(Config other) {
		return config.setValues(other);
	}

	@Override
	@Deprecated(forRemoval = true)
	public boolean setValues(Config other, String... excluded) {
		return config.setValues(other, excluded);
	}

	@Override
	public boolean compareValues(Config other, String... excluded) {
		return config.compareValues(other, excluded);
	}

	@Override
	public String getSeparator() {
		return config.getSeparator();
	}

	@Override
	public String getSaveSeparator() {
		return config.getSaveSeparator();
	}

	@Override
	public @Nullable String getByPath(String path) {
		this.ensureReady();
		return config.getByPath(path);
	}

	@Override
	public @Nullable String get(String... path) {
		this.ensureReady();
		return config.get(path);
	}

	@Override
	public boolean isEmpty() {
		return config.isEmpty();
	}

	@Override
	public Map<String, String> toMap(String separator) {
		return config.toMap(separator);
	}

	@Override
	public boolean validate(SectionValidator validator) {
		return config.validate(validator);
	}

	@Override
	public void load(Object o) {
		this.config.load(o);
	}

	@Override
	public void load(Class<?> c) {
		this.config.load(c);
	}

	@Override
	public int compareTo(@Nullable Config other) {
		return config.compareTo(other);
	}

	@Override
	public @Nullable Node getNodeAt(@NotNull String @NotNull ... steps) {
		this.ensureReady();
		return config.getNodeAt(steps);
	}

	@Override
	public @NotNull Iterator<Node> iterator() {
		this.ensureReady();
		return config.iterator();
	}

}
