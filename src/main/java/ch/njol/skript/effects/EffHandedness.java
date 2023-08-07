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
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Handedness")
@Description("Make mobs left or right-handed. This does not affect players.")
@Examples({
	"spawn skeleton at spawn of world \"world\":",
		"\tmake entity left handed",
	"",
	"make all zombies in radius 10 of player right handed"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper 1.17.1+")
public class EffHandedness extends Effect {

	static {
		if (Skript.methodExists(Mob.class, "setLeftHanded", boolean.class))
			Skript.registerEffect(EffHandedness.class, "make %livingentities% (:left|right)( |-)handed");
	}

	private boolean leftHanded;
	private Expression<LivingEntity> livingEntities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		leftHanded = parseResult.hasTag("left");
		livingEntities = (Expression<LivingEntity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity livingEntity : livingEntities.getArray(event)) {
			if (livingEntity instanceof Mob) {
				((Mob) livingEntity).setLeftHanded(leftHanded);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + livingEntities.toString(event, debug) + " " + (leftHanded ? "left" : "right") + " handed";
	}

}
