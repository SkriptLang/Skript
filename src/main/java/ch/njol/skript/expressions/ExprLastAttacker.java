/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Last Attacker")
@Description("The last block or entity that attacked an entity.")
@Examples({"send \"%last attacker of event-entity%\""})
@Since("2.5.1")
public class ExprLastAttacker extends SimplePropertyExpression<Entity, Object> {
	
	static {
		register(ExprLastAttacker.class, Object.class, "last attacker", "entity");
	}
	
	@Nullable
	private ExprAttacker attackerExpr;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		attackerExpr = new ExprAttacker();
		setExpr((Expression<? extends Entity>) exprs[0]);
		return true;
	}
	
	
	@Nullable
	@Override
	@SuppressWarnings("null")
	public Object convert(Entity entity) {
		return attackerExpr.get(entity.getLastDamageCause())[0];
	}
	
	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "last attacker";
	}
	
}
