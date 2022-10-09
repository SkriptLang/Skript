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
package org.skriptlang.skript.lang.entry;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import org.eclipse.jdt.annotation.Nullable;

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
