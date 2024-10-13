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

import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class EntryNode extends Node implements Entry<String, String> {
	
	private String value;
	
	public EntryNode(final String key, final String value, final String comment, final SectionNode parent, final int lineNum) {
		super(key, comment, parent, lineNum);
		this.value = value;
	}
	
	public EntryNode(final String key, final String value, final SectionNode parent) {
		super(key, parent);
		this.value = value;
	}
	
	@SuppressWarnings("null")
	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public String setValue(final @Nullable String v) {
		if (v == null)
			return value;
		final String r = value;
		value = v;
		return r;
	}
	
	@Override
	String save_i() {
		return key + config.getSaveSeparator() + value;
	}
	
}
