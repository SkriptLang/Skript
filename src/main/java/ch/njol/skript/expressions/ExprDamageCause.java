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

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

@Name("Damage Cause")
@Description("The <a href='./classes.html#damagecause'>damage cause</a> of a damage event. Please click on the link for more information.")
@Examples("damage cause is lava, fire or burning")
@Since("2.0")
public class ExprDamageCause extends EventValueExpression<DamageCause> {
	
	static {
		Skript.registerExpression(ExprDamageCause.class, DamageCause.class, ExpressionType.SIMPLE, "[the] damage (cause|type)");
	}
	
	public ExprDamageCause() {
		super(DamageCause.class);
	}

	@Override
	public boolean setTime(int time) {
		return time != 1; // allow past and present
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the damage cause";
	}
	
}
