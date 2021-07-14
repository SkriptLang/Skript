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

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("BossBar from ID")
@Description("Get an id-based bossbar from its ID")
@RequiredPlugins("1.13+")
@Examples("set style of bossbar from id \"example\" to solid")
@Since("INSERT VERSION")
public class ExprBossBarFromId extends SimpleExpression<BossBar> {
	
	static {
		if (Skript.classExists("org.bukkit.boss.KeyedBossBar"))
			Skript.registerExpression(ExprBossBarFromId.class, BossBar.class, ExpressionType.COMBINED, "[boss[ ]]bar [(from|with) id] %string%");
	}




	@SuppressWarnings("null")
	private Expression<String> id;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		id = (Expression<String>) exprs[0];
		return true;
	}
	
	@Nullable
	@Override
	protected KeyedBossBar[] get(Event e) {
		String id = this.id.getSingle(e);
		return id == null ? null : CollectionUtils.array(Bukkit.getBossBar(NamespacedKey.minecraft(id)));
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends BossBar> getReturnType() {
		return KeyedBossBar.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "bossbar with id " + id.toString(e, debug);
	}
	
}
