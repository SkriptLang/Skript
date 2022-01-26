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

import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Difficulty")
@Description("The difficulty of a world.")
@Examples("set the difficulty of \"world\" to hard")
@Since("2.3")
public class ExprDifficulty extends SimplePropertyExpression<World, Difficulty> {

	static {
		register(ExprDifficulty.class, Difficulty.class, "difficult(y|ies)", "worlds");
	}
	
	@Override
	@Nullable
	public Difficulty convert(World world) {
		return world.getDifficulty();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Difficulty.class);
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;
		
		Difficulty difficulty = (Difficulty) delta[0];
		for (World world : getExpr().getArray(e)) {
			world.setDifficulty(difficulty);
			if (difficulty != Difficulty.PEACEFUL)
				world.setSpawnFlags(true, world.getAllowAnimals()); // Force enable spawn monsters as changing difficulty won't change this by itself
		}
	}
	
	@Override
	protected String getPropertyName() {
		return "difficulty";
	}
	
	@Override
	public Class<Difficulty> getReturnType() {
		return Difficulty.class;
	}

}