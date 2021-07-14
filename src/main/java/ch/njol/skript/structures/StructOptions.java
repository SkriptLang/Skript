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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class StructOptions extends PreloadingStructure {

	static {
		Skript.registerPreloadingStructure(StructOptions.class, 10, "options");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode node) {
		node.convertToEntries(0);
		for (Node n : node) {
			if (!(n instanceof EntryNode)) {
				Skript.error("Invalid line in options");
				continue;
			}
			getParser().getCurrentOptions().put(n.getKey(), ((EntryNode) n).getValue());
		}
		return true;
	}

	@Override
	public void init(SectionNode node) {
		for (Node n : node) {
			// Error in other init method
			if (n instanceof EntryNode)
				getParser().getCurrentOptions().put(n.getKey(), ((EntryNode) n).getValue());
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "options";
	}

}
