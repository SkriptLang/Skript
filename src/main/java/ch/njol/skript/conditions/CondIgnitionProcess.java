/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;

@Name("Ignition Process")
@Description("Checks if a creeper is going to explode.")
@Examples({"if the last spawned creeper is going to explode:",
	"\tloop all players in radius 3 of the last spawned creeper",
	"\t\tsend \"RUN!!!\" to the loop-player"})
@Since("2.5")
@RequiredPlugins("Paper 1.13 or newer")
public class CondIgnitionProcess extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(Creeper.class, "isIgnited")) {
			Skript.registerCondition(CondIgnitionProcess.class,
				"[creeper[s]] %livingentities% ((is|are)|1¦(isn't|is not|aren't|are not)) going to explode",
				"[creeper[s]] %livingentities% ((is|are)|1¦(isn't|is not|aren't|are not)) in the (ignition|explosion) process",
				"creeper[s] %livingentities% ((is|are)|1¦(isn't|is not|aren't|are not)) ignited");
		}
	}

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		setNegated(parseResult.mark == 1);
		return true;
	}

	@Override
	public boolean check(LivingEntity e) {
		return e instanceof Creeper && ((Creeper) e).isIgnited();
	}

	@Override
	protected String getPropertyName() {
		return "going to explode";
	}
}
