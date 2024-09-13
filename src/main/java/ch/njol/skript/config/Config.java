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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a config file.
 * 
 * @author Peter Güttinger
 */
public class Config implements Comparable<Config> {
	
	boolean simple;
	
	/**
	 * One level of the indentation, e.g. a tab or 4 spaces.
	 */
	private String indentation = "\t";
	/**
	 * The indentation's name, i.e. 'tab' or 'space'.
	 */
	private String indentationName = "tab";
	
	final String defaultSeparator;
	String separator;
	
	int level = 0;
	
	private final SectionNode main;
	
	int errors = 0;
	
	final boolean allowEmptySections;
	
	String fileName;
	@Nullable
	Path file = null;
	
	public Config(final InputStream source, final String fileName, @Nullable final File file, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
		try {
			this.fileName = fileName;
			if (file != null) // Must check for null before converting to path
				this.file = file.toPath();
			this.simple = simple;
			this.allowEmptySections = allowEmptySections;
			this.defaultSeparator = defaultSeparator;
			separator = defaultSeparator;
			
			if (source.available() == 0) {
				main = new SectionNode(this);
				Skript.warning("'" + getFileName() + "' is empty");
				return;
			}
			
			if (Skript.logVeryHigh())
				Skript.info("loading '" + fileName + "'");
			
			try (ConfigReader reader = new ConfigReader(source)) {
				main = SectionNode.load(this, reader);
			}
		} finally {
			source.close();
		}
	}
	
	public Config(final InputStream source, final String fileName, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
		this(source, fileName, null, simple, allowEmptySections, defaultSeparator);
	}
	
	public Config(final File file, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
		this(Files.newInputStream(file.toPath()), file.getName(), simple, allowEmptySections, defaultSeparator);
		this.file = file.toPath();
	}
	
	@SuppressWarnings("null")
	public Config(final Path file, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
		this(Channels.newInputStream(FileChannel.open(file)), "" + file.getFileName(), simple, allowEmptySections, defaultSeparator);
		this.file = file;
	}
	
	/**
	 * For testing
	 *
	 * @param s
	 * @param fileName
	 * @param simple
	 * @param allowEmptySections
	 * @param defaultSeparator
	 * @throws IOException
	 */
	public Config(final String s, final String fileName, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
		this(new ByteArrayInputStream(s.getBytes(ConfigReader.UTF_8)), fileName, simple, allowEmptySections, defaultSeparator);
	}

	void setIndentation(final String indent) {
		assert indent != null && !indent.isEmpty() : indent;
		indentation = indent;
		indentationName = (indent.charAt(0) == ' ' ? "space" : "tab");
	}
	
	String getIndentation() {
		return indentation;
	}
	
	String getIndentationName() {
		return indentationName;
	}
	
	public SectionNode getMainNode() {
		return main;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Saves the config to a file.
	 *
	 * @param f The file to save to
	 * @throws IOException If the file could not be written to.
	 */
	public void save(final File f) throws IOException {
		separator = defaultSeparator;
		final PrintWriter w = new PrintWriter(f, "UTF-8");
		try {
			main.save(w);
		} finally {
			w.flush();
			w.close();
		}
	}
	
	/**
	 * Sets this config's values to those in the given config.
	 * <p>
	 * Used by Skript to import old settings into the updated config. The return value is used to not modify the config if no new options were added.
	 *
	 * @param other
	 * @return Whether the configs' keys differ, i.e. false == configs only differ in values, not keys.
	 */
	public boolean setValues(final Config other) {
		return getMainNode().setValues(other.getMainNode());
	}
	
	public boolean setValues(final Config other, final String... excluded) {
		return getMainNode().setValues(other.getMainNode(), excluded);
	}

	/**
	 * Compares the keys and values of this Config and another.
	 * @param other The other Config.
	 * @param excluded Keys to exclude from this comparison.
	 * @return True if there are differences in the keys and their values
	 *  of this Config and the other Config.
	 */
	public boolean compareValues(Config other, String... excluded) {
		return getMainNode().compareValues(other.getMainNode(), excluded);
	}
	
	@Nullable
	public File getFile() {
		if (file != null) {
			try {
				return file.toFile();
			} catch (Exception e) {
				return null; // ZipPath, for example, throws undocumented exception
			}
		}
		return null;
	}
	
	@Nullable
	public Path getPath() {
		return file;
	}
	
	/**
	 * @return The most recent separator. Only useful while the file is loading.
	 */
	public String getSeparator() {
		return separator;
	}
	
	/**
	 * @return A separator string useful for saving, e.g. ": " or " = ".
	 */
	public String getSaveSeparator() {
		if (separator.equals(":"))
			return ": ";
		if (separator.equals("="))
			return " = ";
		return " " + separator + " ";
	}
	
	/**
	 * Splits the given path at the dot character and passes the result to {@link #get(String...)}.
	 *
	 * @param path
	 * @return <tt>get(path.split("\\."))</tt>
	 */
	@SuppressWarnings("null")
	@Nullable
	public String getByPath(final String path) {
		return get(path.split("\\."));
	}
	
	/**
	 * Gets an entry node's value at the designated path
	 *
	 * @param path
	 * @return The entry node's value at the location defined by path or null if it either doesn't exist or is not an entry.
	 */
	@Nullable
	public String get(final String... path) {
		SectionNode section = main;
		for (int i = 0; i < path.length; i++) {
			final Node n = section.get(path[i]);
			if (n == null)
				return null;
			if (n instanceof SectionNode) {
				if (i == path.length - 1)
					return null;
				section = (SectionNode) n;
			} else {
				if (n instanceof EntryNode && i == path.length - 1)
					return ((EntryNode) n).getValue();
				else
					return null;
			}
		}
		return null;
	}
	
	public boolean isEmpty() {
		return main.isEmpty();
	}
	
	public HashMap<String, String> toMap(final String separator) {
		return main.toMap("", separator);
	}
	
	public boolean validate(final SectionValidator validator) {
		return validator.validate(getMainNode());
	}
	
	private void load(final Class<?> cls, final @Nullable Object object, final String path) {
		for (final Field field : cls.getDeclaredFields()) {
			field.setAccessible(true);
			if (object != null || Modifier.isStatic(field.getModifiers())) {
				try {
					if (OptionSection.class.isAssignableFrom(field.getType())) {
						final OptionSection section = (OptionSection) field.get(object);
						@NotNull final Class<?> pc = section.getClass();
						load(pc, section, path + section.key + ".");
					} else if (Option.class.isAssignableFrom(field.getType())) {
						((Option<?>) field.get(object)).set(this, path);
					}
				} catch (final IllegalArgumentException | IllegalAccessException e) {
					assert false;
				}
			}
		}
	}
	
	/**
	 * Sets all {@link Option} fields of the given object to the values from this config
	 */
	public void load(final Object o) {
		load(o.getClass(), o, "");
	}
	
	/**
	 * Sets all static {@link Option} fields of the given class to the values from this config
	 */
	public void load(final Class<?> c) {
		load(c, null, "");
	}

	@Override
	public int compareTo(@Nullable Config other) {
		if (other == null)
			return 0;
		return fileName.compareTo(other.fileName);
	}
	
}
