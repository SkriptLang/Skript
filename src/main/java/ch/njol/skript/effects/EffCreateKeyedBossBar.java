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

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.event.Event;
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
import ch.njol.util.Kleenean;

@Name("Create BossBar with ID")
@Description("Create a new id-based bossbar")
@RequiredPlugins("1.13+")
@Examples({"create bar with id \"example\" and title \"title\"",
	"set (bossbar with id \"example\"'s colour to red"})
@Since("INSERT VERSION")
public class EffCreateKeyedBossBar extends Effect {
	
	static {
		if (Skript.classExists("org.bukkit.boss.KeyedBossBar"))
			Skript.registerEffect(EffCreateKeyedBossBar.class, "create [a] [boss[ ]]bar with id %string% [(and|with)] title[d] %string%");
	}
	
	@SuppressWarnings("null")
	Expression<String> id;
	@SuppressWarnings("null")
	Expression<String> title;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		id = (Expression<String>) exprs[0];
		title = (Expression<String>) exprs[0];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		String id = this.id.getSingle(e);
		if (id == null)
			return;
		String title = this.title.getSingle(e);
		if (title == null)
			title = "BossBar";
		Bukkit.createBossBar(NamespacedKey.minecraft(id), title, BarColor.WHITE, BarStyle.SOLID);
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "create bossbar with id " + id.toString(e, debug) + " with title " + title.toString(e, debug);
	}
	
}
