/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.config;

/**
 * An empty line or a comment.
 * <p>
 * The subclass {@link InvalidNode} is used for invalid non-empty nodes, i.e. where a parsing error occurred.
 *
 * @author Peter Güttinger
 */
public class VoidNode extends Node {

//	private final int initialLevel;
//	private final String initialIndentation;

	VoidNode(final String line, final String comment, final SectionNode parent, final int lineNum) {
		super("" + line.trim(), comment, parent, lineNum);
//		initialLevel = getLevel();
//		initialIndentation = "" + line.replaceFirst("\\S.*$", "");
	}

	@SuppressWarnings("null")
	@Override
	public String getKey() {
		return key;
	}

	public void set(final String s) {
		key = s;
	}

	// doesn't work reliably
//	@Override
//	protected String getIndentation() {
//		int levelDiff = getLevel() - initialLevel;
//		if (levelDiff >= 0) {
//			return StringUtils.multiply(config.getIndentation(), levelDiff) + initialIndentation;
//		} else {
//			final String ci = config.getIndentation();
//			String ind = initialIndentation;
//			while (levelDiff < 0 && ind.startsWith(ci)) {
//				levelDiff++;
//				ind = "" + ind.substring(ci.length());
//			}
//			return ind;
//		}
//	}

	@Override
	String save_i() {
		return "" + key;
	}

}
