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
package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ScriptAliases;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.Script;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.structure.EntryContainer;
import ch.njol.skript.lang.structure.Structure;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class StructAliases extends Structure {

	public static final Priority PRIORITY = new Priority(200);

	static {
		Skript.registerStructure(StructAliases.class, "aliases");
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		SectionNode node = entryContainer.getSource();
		node.convertToEntries(0, "=");

		// Initialize and load script aliases
		Script currentScript = getParser().getCurrentScript();
		assert currentScript != null;
		Aliases.createScriptAliases(currentScript).parser.load(node);

		return true;
	}

	@Override
	public void load() { }

	@Override
	public void unload() {
		// Unload aliases when this Script is unloaded
		Script currentScript = getParser().getCurrentScript();
		assert currentScript != null;
		Aliases.clearScriptAliases(currentScript);
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "aliases";
	}

}
