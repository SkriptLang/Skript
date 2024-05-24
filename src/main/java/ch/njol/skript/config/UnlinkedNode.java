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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * References a node position not in the node tree.
 */
public class UnlinkedNode extends Node implements Map.Entry<String, String> {

	private final String[] path;

	protected UnlinkedNode(Config config, @NotNull String @NotNull... path) {
		super(config);
		if (path.length > 0)
			this.key = path[path.length - 1];
		this.path = path;
	}

	/**
	 * Returns a key/value node at the given path relative to the source.
	 * This might return an {@link EntryNode} or an {@link UnlinkedNode}, depending on whether
	 * such a node exists in the tree.
	 *
	 * @param source The source node
	 * @param path The path of the desired node from the source
	 * @return A key/value node, potentially linked or unlinked
	 * @param <Result> The result type of the node
	 */
	@SuppressWarnings("unchecked")
	public static <Result extends Node & Map.Entry<String, String>>
	@NotNull Result findEntryNode(Node source, @Nullable String path) {
		if (path != null) {
			@Nullable Node node = source.get(path);
			if (node instanceof EntryNode)
				return (Result) node;
		}
		@Nullable String root = source.getPath(), total;
		if (root == null || root.isEmpty())
			total = Objects.toString(path);
		else
			total = root + '.' + path;
		return (Result) new UnlinkedNode(source.config, total.split("\\."));
	}

	@Override
	String save_i() {
		return "";
	}

	@Override
	public @Nullable Node get(String key) {
		return null;
	}

	@Override
	public @Nullable String getValue() {
		return null;
	}

	@Override
	public String setValue(String value) {
		return config.createNode(path, value);
	}

	@Override
	public @Nullable String getPath() {
		return String.join(".", path);
	}

}
