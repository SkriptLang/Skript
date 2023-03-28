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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.entity.Shearable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Random;

@Name("Shear")
@Description("Shears or 'un-shears' a sheep. Please note that no wool is dropped, this only sets the 'sheared' state of the sheep.")
@Examples({"on rightclick on a sheep holding a sword:",
		"	shear the clicked sheep"})
@Since("2.0")
public class EffShear extends Effect {

	private static final boolean interfaceMethod = Skript.classExists("io.papermc.paper.entity.Shearable");
	private static final boolean snowmanMethod = Skript.methodExists(Snowman.class, "setDerp", boolean.class);
	private static final boolean mooshroomMethod = Skript.methodExists(MushroomCow.class, "setVariant", MushroomCow.Variant.class);

	static {
		Skript.registerEffect(EffShear.class,
				(interfaceMethod ? "shear %livingentities% [nodrops:(without drops)]" : "shear %livingentities%"),
				"un[-]shear %livingentities%");
	}
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entity;
	private boolean shear;
	private boolean drops;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entity = (Expression<LivingEntity>) exprs[0];
		shear = matchedPattern == 0;
		drops = !parseResult.hasTag("nodrops");
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entity.getArray(event)) {
			if (shear && interfaceMethod) {
				if (drops && entity instanceof Shearable) {
					((Shearable) entity).shear();
				}
			}
			if (entity instanceof Sheep) {
				((Sheep) entity).setSheared(shear);
			} else if (mooshroomMethod) {
				if (!shear && entity instanceof MushroomCow) {
					int rng = new Random().nextInt(MushroomCow.Variant.values().length);
					((MushroomCow) entity).setVariant(MushroomCow.Variant.values()[rng]);
				}
			} else if (snowmanMethod) {
				if (!shear && entity instanceof Snowman) {
					((Snowman) entity).setDerp(false);
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (shear ? "" : "un") + "shear " + entity.toString(event, debug) + (interfaceMethod && drops ? " with drops" : "");
	}
	
}
