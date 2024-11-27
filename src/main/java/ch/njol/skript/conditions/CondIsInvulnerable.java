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
import ch.njol.skript.doc.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;

@Name("Is Invulnerable")
@Description("Checks whether an entity or a gamemode is invulnerable.\nFor gamemodes, Paper and Minecraft 1.20.6 are required")
@Examples({
	"target entity is invulnerable",
	"",
	"loop all gamemodes:",
		"\tif loop-value is not invulnerable:",
			"\t\tbroadcast \"the gamemode %loop-value% is vulnerable!\""
})
@Since("2.5, INSERT VERSION (gamemode)")
@RequiredPlugins("Paper 1.20.6+ (gamemodes)")
public class CondIsInvulnerable extends PropertyCondition<Object> {
	private static final boolean SUPPORTS_GAMEMODE = Skript.methodExists(GameMode.class, "isInvulnerable");
	
	static {
		register(CondIsInvulnerable.class, PropertyType.BE, "(invulnerable|invincible)", "entities" + (SUPPORTS_GAMEMODE ? "/gamemodes" : ""));
	}
	
	@Override
	public boolean check(Object object) {
		if (object instanceof Entity) {
			return ((Entity) object).isInvulnerable();
		} else if (SUPPORTS_GAMEMODE && object instanceof GameMode) {
			return ((GameMode) object).isInvulnerable();
		}
		return false;
	}
	
	@Override
	protected String getPropertyName() {
		return "invulnerable";
	}
	
}
