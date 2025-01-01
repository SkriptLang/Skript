package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.log.SkriptLogger;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Validated;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a config file.
 */
public class Config implements Comparable<Config>, Validated, NodeNavigator, AnyNamed {

	/**
	 * One level of the indentation, e.g. a tab or 4 spaces.
	 */
	private String indentation = "\t";

	/**
	 * The indentation's name, i.e. 'tab' or 'space'.
	 */
	private String indentationName = "tab";

	private final SectionNode main;

	final String defaultSeparator;
	String separator;
	boolean simple;
	int level = 0;
	int errors = 0;
	final boolean allowEmptySections;

	String fileName;
	@Nullable Path file = null;
	private final Validated validator = Validated.validator();

	public Config(InputStream source, String fileName, @Nullable File file,
				  boolean simple, boolean allowEmptySections, String defaultSeparator) throws IOException {
		try (source) {
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
		}
	}

	public Config(InputStream source, String fileName, boolean simple,
				  boolean allowEmptySections, String defaultSeparator) throws IOException {
		this(source, fileName, null, simple, allowEmptySections, defaultSeparator);
	}

	public Config(File file, boolean simple,
				  boolean allowEmptySections, String defaultSeparator) throws IOException {
		this(Files.newInputStream(file.toPath()), file.getName(), simple, allowEmptySections, defaultSeparator);
		this.file = file.toPath();
	}

	public Config(@NotNull Path file, boolean simple,
				  boolean allowEmptySections, String defaultSeparator) throws IOException {
		this(Channels.newInputStream(FileChannel.open(file)), "" + file.getFileName(), simple, allowEmptySections, defaultSeparator);
		this.file = file;
	}

	/**
	 * Sets all {@link Option} fields of the given object to the values from this config
	 */
	public void load(Object object) {
		load(object.getClass(), object, "");
	}

	/**
	 * A dummy config with no (known) content.
	 */
	@ApiStatus.Internal
	public Config(String fileName, @Nullable final File file) {
		this.fileName = fileName;
		if (file != null)
			this.file = file.toPath();
		this.simple = false;
		this.allowEmptySections = false;
		this.separator = defaultSeparator = "";
		this.main = new SectionNode(this);
		SkriptLogger.setNode(null); // clean-up after section node
	}

	/**
	 * Sets all static {@link Option} fields of the given class to the values from this config
	 */
	public void load(Class<?> clazz) {
		load(clazz, null, "");
	}

	private void load(Class<?> clazz, @Nullable Object object, String path) {
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (object != null || Modifier.isStatic(field.getModifiers())) {
				try {
					if (OptionSection.class.isAssignableFrom(field.getType())) {
						OptionSection section = (OptionSection) field.get(object);
						@NotNull Class<?> pc = section.getClass();
						load(pc, section, path + section.key + ".");
					} else if (Option.class.isAssignableFrom(field.getType())) {
						((Option<?>) field.get(object)).set(this, path);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					assert false;
				}
			}
		}
	}

	void setIndentation(String indent) {
		assert indent != null && !indent.isEmpty() : indent;
		indentation = indent;
		indentationName = indent.charAt(0) == ' ' ? "space" : "tab";
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
	 * @param file The file to save to
	 * @throws IOException If the file could not be written to.
	 */
	public void save(File file) throws IOException {
		this.separator = defaultSeparator;
		try (final PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
			this.main.save(writer);
			writer.flush();
		}
	}

	/**
	 * @deprecated This copies all values from the other config and sets them in this config,
	 * which could be destructive for sensitive data if something goes wrong.
	 * Also removes user comments.
	 * Use {@link #updateNodes(Config)} instead.
	 */
	@Deprecated(forRemoval = true)
	public boolean setValues(final Config other) {
		return getMainNode().setValues(other.getMainNode());
	}

	/**
	 * @deprecated This copies all values from the other config and sets them in this config,
	 * which could be destructive for sensitive data if something goes wrong.
	 * Also removes user comments.
	 * Use {@link #updateNodes(Config)} instead.
	 */
	@Deprecated(forRemoval = true)
	public boolean setValues(final Config other, final String... excluded) {
		return getMainNode().setValues(other.getMainNode(), excluded);
	}

	/**
	 * Updates the nodes of this config with the nodes of another config.
	 * Used for updating a config file to a newer version.
	 * <p>
	 * This method only sets nodes that are missing in this config, thus preserving any existing values.
	 * </p>
	 *
	 * @param newer The newer config to update from.
	 * @return True if any keys were added to this config, false otherwise.
	 */
	public boolean updateNodes(@NotNull Config newer) {
		Skript.debug("Updating config %s", newer.getFileName());
		Set<Node> newNodes = discoverNodes(newer.getMainNode());
		Set<Node> oldNodes = discoverNodes(getMainNode());

		newNodes.removeAll(oldNodes);
		Set<Node> nodesToUpdate = new LinkedHashSet<>(newNodes);

		if (nodesToUpdate.isEmpty())
			return false;

		for (Node node : nodesToUpdate) {
			Skript.debug("Updating node %s", node);
			SectionNode newParent = node.getParent();
			Preconditions.checkNotNull(newParent);

			SectionNode parent = getNode(newParent.getPathSteps());
			Preconditions.checkNotNull(parent);

			int index = node.getIndex();
			if (index >= parent.size()) {
				Skript.debug("Adding node %s to %s (size mismatch)", node, parent);
				parent.add(node);
				continue;
			}

			Node existing = parent.getAt(index);
			if (existing != null) { // insert between existing
				Skript.debug("Adding node %s to %s at index %s", node, parent, index);
				parent.add(index, node);
			} else {
				Skript.debug("Adding node %s to %s", node, parent);
				parent.add(node);
			}
		}
		return true;
	}

	/**
	 * Recursively finds <i>all</i> nodes in a section node, including other
	 * section nodes, entry nodes, and void nodes.
	 *
	 * @param node The parent node to search.
	 * @return A set of the discovered nodes, guaranteed to be in the order of discovery.
	 */
	@Contract(pure = true)
	static @NotNull Set<Node> discoverNodes(@NotNull SectionNode node) {
		Set<Node> nodes = new LinkedHashSet<>();

		for (Iterator<Node> iterator = node.fullIterator(); iterator.hasNext(); ) {
			Node child = iterator.next();
			if (child instanceof SectionNode sectionChild) {
				nodes.add(child);
				nodes.addAll(discoverNodes(sectionChild));
			} else if (child instanceof EntryNode || child instanceof VoidNode) {
				nodes.add(child);
			}
		}
		return nodes;
	}

	/**
	 * Returns the {@link SectionNode} at the given path from the root,
	 * where {@code path} is an array of keys to traverse.
	 *
	 * @param path The path to the node.
	 * @return The {@link SectionNode} at the given path.
	 */
	private SectionNode getNode(String... path) {
		SectionNode node = getMainNode();
		for (String key : path) {
			Node child = node.get(key);

			if (child instanceof SectionNode sectionNode) {
				node = sectionNode;
			} else {
				return node;
			}
		}
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

	/**
	 * Splits the given path at the dot character and passes the result to {@link #get(String...)}.
	 *
	 * @param path
	 * @return <tt>get(path.split("\\."))</tt>
	 */
	public @Nullable String getByPath(@NotNull String path) {
		return get(path.split("\\."));
	}

	/**
	 * Gets an entry node's value at the designated path
	 *
	 * @param path
	 * @return The entry node's value at the location defined by path or null if it either doesn't exist or is not an entry.
	 */
	public @Nullable String get(String... path) {
		SectionNode section = main;
		for (int i = 0; i < path.length; i++) {
			Node node = section.get(path[i]);
			if (node == null)
				return null;
			if (node instanceof SectionNode sectionNode) {
				if (i == path.length - 1)
					return null;
				section = sectionNode;
			} else {
				if (node instanceof EntryNode entryNode && i == path.length - 1)
					return entryNode.getValue();
				else
					return null;
			}
		}
		return null;
	}

	public Map<String, String> toMap(String separator) {
		return main.toMap("", separator);
	}

	public boolean validate(SectionValidator validator) {
		return validator.validate(getMainNode());
	}

	/**
	 * @return Whether the config is empty.
	 */
	public boolean isEmpty() {
		return main.isEmpty();
	}

	/**
	 * @return The file this config was loaded from, or null if it was loaded from an InputStream.
	 */
	public @Nullable File getFile() {
		if (file == null)
			return null;

		try {
			return file.toFile();
		} catch (Exception e) {
			return null; // ZipPath, for example, throws undocumented exception
		}
	}

	/**
	 * @return The path this config was loaded from, or null if it was loaded from an InputStream.
	 */
	public @Nullable Path getPath() {
		return file;
	}

	/**
	 * @return The most recent separator. Only useful while the file is loading.
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * @return A separator string useful for saving, e.g. ": ".
	 */
	public String getSaveSeparator() {
		if (separator.equals(":"))
			return ": ";
		return " " + separator + " ";
	}

	@Override
	public int compareTo(@Nullable Config other) {
		if (other == null)
			return 0;
		return fileName.compareTo(other.fileName);
	}

	@Override
	public void invalidate() {
		this.validator.invalidate();
	}

	@Override
	public boolean valid() {
		return validator.valid();
	}

	@Override
	public @NotNull Node getCurrentNode() {
		return main;
	}

	@Override
	public @Nullable Node getNodeAt(@NotNull String @NotNull ... steps) {
		return main.getNodeAt(steps);
	}

	@NotNull
	@Override
	public Iterator<Node> iterator() {
		return main.iterator();
	}

	@Override
	public @Nullable Node get(String step) {
		return main.get(step);
	}

	/**
	 * @return The name of this config (excluding path and file extensions)
	 */
	@Override
	public String name() {
		String name = this.getFileName();
		if (name == null)
			return null;
		if (name.contains(File.separator))
			name = name.substring(name.lastIndexOf(File.separator) + 1);
		if (name.contains("."))
			return name.substring(0, name.lastIndexOf('.'));
		return name;
	}

}
