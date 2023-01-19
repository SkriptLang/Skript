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

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Leash entities")
@Description("Leash living entities to other entities.")
@Examples("leash the player to the target entity")
@Since("2.3")
public class EffLeash extends Effect {

	static {
		Skript.registerEffect(EffLeash.class,
			"(leash|lead) %livingentities% to %entity%",
			"make %entity% (leash|lead) %livingentities%",
			"un(leash|lead) [holder of] %livingentities%");
	}

	@SuppressWarnings("null")
	private Expression<Entity> holder;
	@SuppressWarnings("null")
	private Expression<LivingEntity> targets;
	private boolean leash;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		leash = matchedPattern != 2;
		if (leash) {
			holder = (Expression<Entity>) exprs[1 - matchedPattern];
			targets = (Expression<LivingEntity>) exprs[matchedPattern];
		} else {
			targets = (Expression<LivingEntity>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (leash) {
			Entity holder = this.holder.getSingle(event);
			if (holder == null)
				return;
			for (LivingEntity target : targets.getArray(event))
				target.setLeashHolder(holder);
		} else {
			for (LivingEntity target : targets.getArray(event))
				target.setLeashHolder(null);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (leash)
			return "leash " + targets.toString(event, debug) + " to " + holder.toString(event, debug);
		else
			return "unleash " + targets.toString(event, debug);
	}

}
