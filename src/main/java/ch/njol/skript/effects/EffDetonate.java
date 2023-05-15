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
import org.bukkit.entity.Firework;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Detonate Fireworks")
@Description("Forces all fireworks to explode immediately, as if it has no remaining fuse.")
@Examples("detonate all fireworks")
@Since("INSERT VERSION")
public class EffDetonate extends Effect {

	static {
		Skript.registerEffect(EffDetonate.class, "detonate %projectiles%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Projectile> projectiles;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		projectiles = ((Expression<Projectile>) exprs[0]);
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Projectile firework : projectiles.getArray(event)) {
			if (!(firework instanceof Firework))
				continue;
			((Firework) firework).detonate();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "detonate " + projectiles.toString(event, debug);
	}

}
