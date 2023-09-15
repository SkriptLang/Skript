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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import io.papermc.paper.world.MoonPhase;
import org.bukkit.World;
import org.eclipse.jdt.annotation.Nullable;

@Name("Moon Phase")
@Description("The current moon phase of a world.")
@Examples({
	"if moon phase of player's world is full moon:",
	"\tsend \"Watch for the wolves!\""
})
@Since("2.7")
@RequiredPlugins("Paper 1.16+")
public class ExprMoonPhase extends SimplePropertyExpression<World, MoonPhase> {

	static {
		if (Skript.classExists("io.papermc.paper.world.MoonPhase"))
			register(ExprMoonPhase.class, MoonPhase.class, "(lunar|moon) phase[s]", "worlds");
	}

	@Override
	@Nullable
	public MoonPhase convert(World world) {
		return world.getMoonPhase();
	}

	@Override
	public Class<? extends MoonPhase> getReturnType() {
		return MoonPhase.class;
	}

	@Override
	protected String getPropertyName() {
		return "moon phase";
	}

}
