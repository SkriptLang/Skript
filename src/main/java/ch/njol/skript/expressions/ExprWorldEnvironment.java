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
package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.eclipse.jdt.annotation.Nullable;

@Name("World Environment")
@Description("The environment of a world")
@Examples({
	"if environment of player's world is nether:",
	"\tapply fire resistance to player for 10 minutes"
})
@Since("2.7")
public class ExprWorldEnvironment extends SimplePropertyExpression<World, Environment> {

	static {
		register(ExprWorldEnvironment.class, Environment.class, "[world] environment", "worlds");
	}

	@Override
	@Nullable
	public Environment convert(World world) {
		return world.getEnvironment();
	}

	@Override
	public Class<? extends Environment> getReturnType() {
		return Environment.class;
	}

	@Override
	protected String getPropertyName() {
		return "environment";
	}

}
