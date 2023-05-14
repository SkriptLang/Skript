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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Projectile;

@Name("Firework Is Detonated")
@Description("Checks whether a firework has been detonated.")
@Examples({
	"while {_firework} has not been detonated:",
		"\tspawn tnt at {_firework}",
		"\twait 10 ticks"
})
@Since("INSERT VERSION")
public class CondFireworkIsDetonated extends PropertyCondition<Projectile> {

	static {
		Skript.registerCondition(CondFireworkIsDetonated.class, "%projectiles% (had|has|have) [:not] [been] detonated");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.hasTag("not"));
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Projectile projectile) {
		if (!(projectile instanceof Firework))
			return false;
		return ((Firework) projectile).isDetonated();
	}

	@Override
	protected String getPropertyName() {
		return "detonated";
	}
}
