package org.skriptlang.skript.lang.entry;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import org.jetbrains.annotations.Nullable;

/**
 * A simple entry data class for handling {@link SectionNode}s.
 */
public class SectionEntryData extends EntryData<SectionNode> {

	public SectionEntryData(String key, @Nullable SectionNode defaultValue, boolean optional) {
		super(key, defaultValue, optional);
	}

	/**
	 * Because this entry data is for {@link SectionNode}s, no specific handling needs to be done to obtain the "value".
	 * This method just asserts that the provided node is actually a {@link SectionNode}.
	 * @param node A {@link SimpleNode} to obtain (and possibly convert) the value of.
	 * @return The value obtained from the provided {@link SimpleNode}.
	 */
	@Override
	@Nullable
	public SectionNode getValue(Node node) {
		assert node instanceof SectionNode;
		return (SectionNode) node;
	}

	/**
	 * Checks whether the provided node can be used as the section for this entry data.
	 * A check is done to verify that the node is a {@link SectionNode}, and that it also
	 *  meets the requirements of {@link EntryData#canCreateWith(Node)}.
	 * @param node The node to check.
	 * @return Whether the provided {@link Node} works with this entry data.
	 */
	@Override
	public boolean canCreateWith(Node node) {
		if (!(node instanceof SectionNode))
			return false;
		String key = node.getKey();
		if (key == null)
			return false;
		key = ScriptLoader.replaceOptions(key);
		return getKey().equalsIgnoreCase(key);
	}

}
