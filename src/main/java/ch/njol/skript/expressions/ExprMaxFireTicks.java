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

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Name("Maximum Time On Fire")
@Description("Returns the maximum amount of time before an entity begins getting damaged by fire.")
@Examples({
	"send target's max time on fire"
})
@Since("INSERT VERSION")
public class ExprMaxFireTicks extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprMaxFireTicks.class, Timespan.class, "max[imum] time on fire", "entities");
	}

	@Override
	@Nullable
	public Timespan convert(Entity entity) {
		return Timespan.fromTicks(entity.getMaxFireTicks());
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "maximum time on fire";
	}

}
