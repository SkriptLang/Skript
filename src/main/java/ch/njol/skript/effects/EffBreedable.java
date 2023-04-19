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
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Make Breedable")
@Description({
		"Picks whether or not entities will be able to breed.",
		"Only works on animals, not all living entities"
})
@Examples({
	"on spawn of animal:",
		"\tmake entity unbreedable"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.16+")
public class EffBreedable extends Effect {

	static {
		if (Skript.classExists("org.bukkit.entity.Breedable"))
			Skript.registerEffect(EffBreedable.class, "make %livingentities% [negate:(not |non(-| )|un)]breedable");
	}

	private boolean canBreed;
	private Expression<LivingEntity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		canBreed = !parseResult.hasTag("negate");
		entities = (Expression<LivingEntity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity livingEntity : entities.getArray(event)) {
			if (livingEntity instanceof Breedable) {
				((Breedable) livingEntity).setBreed(canBreed);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (canBreed ? " " : " non-") + "breedable";
	}

}
