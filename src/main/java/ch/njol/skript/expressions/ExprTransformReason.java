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
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

@Name("Transform Reason")
@Description("The <a href='classes.html#transformreason'>transform reason</a> within an entity <a href='events.html#entity transform'>entity transform</a> event.")
@Examples({
	"on entity transform:",
		"\ttransform reason is infection, drowned or frozen"
})
@Since("2.8.0")
public class ExprTransformReason extends EventValueExpression<TransformReason> {

	static {
		Skript.registerExpression(ExprTransformReason.class, TransformReason.class, ExpressionType.SIMPLE, "[the] transform[ing] (cause|reason|type)");
	}

	public ExprTransformReason() {
		super(TransformReason.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "transform reason";
	}

}
