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

@Name("Lock Age")
@Description("Picks whether or not an entity will be able to age or mate.")
@Examples({
	"on spawn of animal:",
	"\tlock age of entity"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.16+")
public class EffLockAge extends Effect {

	static {
		if (Skript.classExists("org.bukkit.entity.Breedable"))
			Skript.registerEffect(EffLockAge.class, "(lock|:unlock) age of %livingentities%");
	}

	private Expression<LivingEntity> entities;
	private boolean unlock;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		unlock = parseResult.hasTag("unlock");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity livingEntity : entities.getArray(event)) {
			if (livingEntity instanceof Breedable) {
				((Breedable) livingEntity).setAgeLock(!unlock);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (unlock ? "unlock" : "lock") + " age of " + entities.toString(event,debug);
	}

}
