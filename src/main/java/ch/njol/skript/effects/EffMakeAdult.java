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
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Make Adult")
@Description("Force a animal to become an adult or baby, requires MC 1.16 for any mob")
@Examples({
	"on spawn of mob:",
	"\tif entity is not an adult:",
	"\t\tmake entity an adult",
})
@Since("INSERT VERSION")
@RequiredPlugins("1.16+ (Mobs)")
public class EffMakeAdult extends Effect {


	// This is required since before 1.16 the `setBaby`/'setAdult' method only supported Animals
	private final static boolean HAS_MOB_SUPPORT = Skript.isRunningMinecraft(1,16,5);

	static {
		Skript.registerEffect(EffMakeAdult.class, "make %livingentities% [a[n]] (adult|:baby)");
	}

	private boolean makeBaby;
	private Expression<LivingEntity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		makeBaby = parseResult.hasTag("baby");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity livingEntity : entities.getArray(event)) {
			if (livingEntity instanceof Ageable) {
				if (!HAS_MOB_SUPPORT && !(livingEntity instanceof Animals))
					continue;
				if (makeBaby) {
					((Ageable) livingEntity).setBaby();
				} else {
					((Ageable) livingEntity).setAdult();
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities + (makeBaby ? " a baby" : " an adult");
	}

}
