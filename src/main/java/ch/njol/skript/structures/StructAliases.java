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
import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class StructAliases extends Structure {

	static {
		Skript.registerStructure(StructAliases.class, "aliases");
		ParserInstance.registerData(AliasesData.class, AliasesData::new);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode node) {
		node.convertToEntries(0, "=");

		// Initialize and load script aliases
		ScriptAliases aliases = Aliases.createScriptAliases();
		Aliases.setScriptAliases(aliases);
		aliases.parser.load(node);
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "aliases";
	}

	public static class AliasesData extends ParserInstance.Data {
		public AliasesData(ParserInstance parserInstance) {
			super(parserInstance);
		}

		@Override
		public void onCurrentScriptChange(@Nullable Config currentScript) {
			Aliases.setScriptAliases(null); // These are per-script
		}
	}

}
