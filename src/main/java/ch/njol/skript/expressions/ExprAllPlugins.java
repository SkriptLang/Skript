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
package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.plugin.Plugin;


@Name("All plugins")
@Description("Gets a list of all currently loaded plugins.")
@Examples({"if all plugins contains \"Vault\":",
		"send \"Plugins (%size of all plugins%): %all plugins%\" to player"})
@Since("INSERT VERSION")
public class ExprAllPlugins extends SimpleExpression<Plugin> {
	
	static {
		Skript.registerExpression(ExprAllPlugins.class, Plugin.class, ExpressionType.SIMPLE, "all [loaded] plugins");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	protected @Nullable Plugin[] get(Event e) {
		return Bukkit.getPluginManager().getPlugins();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Plugin> getReturnType() {
		return Plugin.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "all plugins";
	}

}
