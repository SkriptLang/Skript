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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Make Invisible")
@Description("Makes a living entity visible/invisible.")
@Examples("make target entity invisible")
@Since("INSERT VERSION")
public class EffInvisible extends Effect {

	static {
		if (Skript.methodExists(LivingEntity.class, "isInvisible"))
			Skript.registerEffect(EffInvisible.class,
				"make %livingentities% (invisible|not visible)",
				"make %livingentities% (visible|not invisible)");
	}

	private Expression<LivingEntity> livingEntities;
	private boolean invisible;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		livingEntities = (Expression<LivingEntity>) exprs[0];
		invisible = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (LivingEntity entity : livingEntities.getArray(e))
			entity.setInvisible(invisible);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "make " + livingEntities.toString(e, debug) + " " + (invisible ? "in" : "") + "visible";
	}

}
