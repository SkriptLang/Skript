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
package ch.njol.skript.conditions;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.structure.StructureManager;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

@Name("Structure Exists")
@Description("Check if structures exist.")
@Examples("if structure named \"Example\" does exist")
@RequiredPlugins("Minecraft 1.17.1+")
@Since("INSERT VERSION")
public class CondStructureExists extends Condition {

	static {
		if (Skript.classExists("org.bukkit.structure.Structure"))
			Skript.registerCondition(CondStructureExists.class,
					"structure[s] [named] %strings% [do[es]] exist[s]",
					"structure[s] [named] %strings% (do[es]n't|do[es] not) exist"
			);
	}

	private Expression<String> names;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		names = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		StructureManager manager = Bukkit.getStructureManager();
		return names.check(event, name -> {
			NamespacedKey key = Utils.getNamespacedKey(name);
			if (key == null)
				return false;
			return manager.loadStructure(key, false) != null;
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "structures " + names.toString(event, debug) + (names.isSingle() ? " does " : " do ") + (isNegated() ? "not " : "") + " exist";
	}

}
