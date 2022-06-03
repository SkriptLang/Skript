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
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.Script;
import ch.njol.skript.lang.Script.ScriptEventHandler;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.structure.EntryContainer;
import ch.njol.skript.lang.structure.Structure;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StructOptions extends Structure {

	public static final Priority PRIORITY = new Priority(100);

	static {
		Skript.registerStructure(StructOptions.class, "options");
	}

	private final Map<String, String> options = new HashMap<>();

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		SectionNode node = entryContainer.getSource();
		node.convertToEntries(-1);
		loadOptions(node, "");

		Script currentScript = getParser().getCurrentScript();
		assert currentScript != null;
		currentScript.addEventHandler(new ScriptEventHandler() {
			@Override
			public void onLoad(@Nullable Script oldScript) {
				HashMap<String, String> currentOptions = getParser().getCurrentOptions();
				currentOptions.clear(); // Clear it just to be safe
				currentOptions.putAll(options);
			}

			@Override
			public void onUnload(@Nullable Script newScript) {
				getParser().getCurrentOptions().clear();
			}
		});

		return true;
	}

	@Override
	public void load() { }

	private void loadOptions(SectionNode sectionNode, String prefix) {
		for (Node n : sectionNode) {
			if (n instanceof EntryNode) {
				options.put(prefix + n.getKey(), ((EntryNode) n).getValue());
			} else if (n instanceof SectionNode) {
				loadOptions((SectionNode) n, prefix + n.getKey() + ".");
			} else {
				Skript.error("Invalid line in options");
			}
		}
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "options";
	}

}
