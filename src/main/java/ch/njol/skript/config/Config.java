package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.config.validate.SectionValidator;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents a config file.
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
	 * @deprecated This copies all values from the other config and sets them in this config,
	 * which could be destructive for sensitive data if something goes wrong.
	 * Use {@link #updateKeys(Config)} instead.
	 */
	@Deprecated(forRemoval = true)
	public boolean setValues(final Config other) {
		return getMainNode().setValues(other.getMainNode());
	}

	/**
	 * @deprecated This copies all values from the other config and sets them in this config,
	 * which could be destructive for sensitive data if something goes wrong.
	 * Use {@link #updateKeys(Config)} instead.
	 */
	@Deprecated(forRemoval = true)
	public boolean setValues(final Config other, final String... excluded) {
		return getMainNode().setValues(other.getMainNode(), excluded);
	}

	/**
	 * Updates the keys of this config with the keys of another config.
	 * Used for updating a config file to a newer version.
	 * This method only sets keys that are missing in this config, thus preserving any existing values.
	 *
	 * @param newer The newer config to update from.
	 * @return True if any keys were added to this config, false otherwise.
	 */
	public boolean updateKeys(@NotNull Config newer) {
		Set<String> newKeys = findKeys(newer.getMainNode(), "");
		Set<String> oldKeys = findKeys(getMainNode(), "");

		newKeys.removeAll(oldKeys);
		Set<String> missingKeys = new LinkedHashSet<>(newKeys);

		if (missingKeys.isEmpty())
			return false;

		for (String key : missingKeys) {
			Node node = newer.getNode(key);
			if (!(node instanceof EntryNode entryNode))
				continue;

			updateEntry(key, entryNode);
		}
		return true;
	}

	// TODO COPY VOIDNODES RELATIVE TO
	private void updateEntry(@NotNull String key, @NotNull EntryNode node) {
		int idx = node.getIndex();
		int splitAt = key.lastIndexOf('.');
		if (splitAt == -1) { // top level key
			getMainNode().add(idx, new EntryNode(key, node.getValue(), node.getComment(), getMainNode(), idx));
			return;
		}

		String pathToKey = key.substring(0, splitAt);
		String leafKey = key.substring(splitAt + 1); // exclude .

		Node parent = getNode(pathToKey);

		if (parent == null) // parent section does not exist, so check all ancestors and add them if missing
			parent = addMissingAncestors(pathToKey.split("\\.")).getLast();

		if (parent instanceof SectionNode parentSection) {
			parentSection.add(idx, new EntryNode(leafKey, node.getValue(), node.getComment(), parentSection, -1));
		}
	}

	/**
	 * Adds possibly missing ancestor nodes specified in {@code keys}.
	 *
	 * @param keys An array of keys to add as ancestors.
	 * @return A list of the added nodes.
	 */
	private List<Node> addMissingAncestors(String[] keys) {
		List<Node> added = new ArrayList<>();
		SectionNode parent = getMainNode();

		for (String key : keys) {
			Node node = parent.get(key);
			if (node == null) {
				// TODO COMMENT SUPPORT
				SectionNode newNode = new SectionNode(key, "", parent, -1);
				parent.add(newNode);
				added.add(newNode);
			} else {
				Preconditions.checkArgument(node instanceof SectionNode, "node is not a section node");
				parent = (SectionNode) node;
			}
		}

		return added;
	}

	/**
	 * Recursively finds all keys in a section node.
	 * <p>Keys are represented in dot notation, e.g. {@code grandparent.parent.child}.</p>
	 *
	 * @param node The parent node to search.
	 * @param key  The built key of the current node.
	 *             Should be empty when calling this method outside the method itself.
	 * @return A set of the discovered keys, guaranteed to be in the order of discovery.
	 */
	@Contract(pure = true)
	private static Set<String> findKeys(@NotNull SectionNode node, @NotNull String key) {
		Set<String> keys = new LinkedHashSet<>();

		if (!key.isEmpty()) {
			key += ".";
		}

		for (Node child : node) {
			if (child instanceof SectionNode sectionNode) {
				keys.addAll(findKeys(sectionNode, key + sectionNode.getKey()));
			} else if (child instanceof EntryNode entryNode) {
				keys.add(key + entryNode.getKey());
			}
		}
		return keys;
	}

	/**
	 * Gets a node at the given path, split by dot characters. May be null.
	 *
	 * @param path The path to the section node.
	 * @return The node at the given path or null if it does not exist.
	 */
	public @Nullable Node getNode(@NotNull String path) {
		return getNode(main, new LinkedList<>(List.of(path.split("\\."))));
	}

	/**
	 * Gets a node at the given path. May be null.
	 *
	 * @param path The path to the section node.
	 * @return The node at the given path or null if it does not exist.
	 */
	public @Nullable Node getNode(@NotNull String... path) {
		return getNode(main, new LinkedList<>(List.of(path)));
	}

	/**
	 * Recursively gets a node at the given path. May be null.
	 *
	 * @param section The current section node to search.
	 * @param path    The remaining path to the desired node.
	 * @return The node at the given path or null if it does not exist.
	 * @see #getNode(String)
	 * @see #getNode(String...)
	 */
	private @Nullable Node getNode(@NotNull SectionNode section, @NotNull Queue<String> path) {
		String head = path.poll();
		if (head == null)
			return section;

		Node node = section.get(head);
		if (node instanceof SectionNode sectionNode)
			return getNode(sectionNode, path);

		return node;
	}

	/**
	 * Compares the keys and values of this Config and another.
	 *
	 * @param other    The other Config.
	 * @param excluded Keys to exclude from this comparison.
	 * @return True if there are differences in the keys and their values
	 * of this Config and the other Config.
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
