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
package ch.njol.skript.effects;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

@Name("Structure Save/Unregister")
@Description("Unregisters or saves a structure by it's namespace key. Unregister will unload if the structure was loaded vs Delete.")
@Examples("unregister structure named \"Example\"")
@RequiredPlugins("Spigot 1.17.1+")
@Since("INSERT VERSION")
public class EffStructureSaveUnregister extends Effect {

	static {
		if (Skript.classExists("org.bukkit.structure.Structure"))
			Skript.registerEffect(EffStructureSaveUnregister.class, "(:unregister|delete) structure[s] [with name|named] %strings%", "save [structure] %structure% [with name|named] %string%");
	}

	private Expression<Structure> structure;
	private Expression<String> names;
	private boolean save, unregister;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		names = (Expression<String>) exprs[matchedPattern];
		unregister = parseResult.hasTag("unregister");
		if (save = matchedPattern == 1)
			structure = (Expression<Structure>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		StructureManager manager = Bukkit.getStructureManager();
		if (save) {
			String name = this.names.getSingle(event);
			if (name == null)
				return;
			Structure structure = this.structure.getSingle(event);
			if (structure == null)
				return;
			try {
				manager.saveStructure(Utils.getNamespacedKey(name), structure);
			} catch (IOException e) {
				Skript.exception(e, "Failed to save structure " + name);
			}
		} else {
			for (String name : names.getArray(event)) {
				NamespacedKey key = Utils.getNamespacedKey(name);
				if (key == null)
					continue;
				try {
					manager.deleteStructure(key, unregister);
				} catch (IOException e) {
					Skript.exception(e, "Failed to delete structure " + name);
				}
			}
			
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (save ? "save" : "delete") + " structures " + names.toString(event, debug);
	}

}
