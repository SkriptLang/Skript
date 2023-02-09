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
package ch.njol.skript.entity;

import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;

public class WolfData extends EntityData<Wolf> {

	static {
		EntityData.register(WolfData.class, "wolf", Wolf.class, 1,
				"peaceful wolf", "wolf", "angry wolf",
				"wild wolf", "tamed wolf");
	}

	@Nullable
	private DyeColor collarColor;

	private int angry = 0;
	private int tamed = 0;

	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern <= 2)
			angry = matchedPattern - 1;
		else
			tamed = matchedPattern == 3 ? -1 : 1;
		if (exprs[0] != null)
			collarColor = ((Literal<Color>) exprs[0]).getSingle().asDyeColor();
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Wolf> c, @Nullable Wolf wolf) {
		if (wolf != null) {
			angry = wolf.isAngry() ? 1 : -1;
			tamed = wolf.isTamed() ? 1 : -1;
			collarColor = wolf.getCollarColor();
		}
		return true;
	}

	@Override
	public void set(Wolf entity) {
		if (angry != 0)
			entity.setAngry(angry == 1);
		if (tamed != 0)
			entity.setTamed(tamed == 1);
		if (collarColor != null)
			entity.setCollarColor(collarColor);
	}

	@Override
	public boolean match(Wolf entity) {
		return (angry == 0 || entity.isAngry() == (angry == 1)) && (tamed == 0 || entity.isTamed() == (tamed == 1)) && (collarColor == null ? true : entity.getCollarColor() == collarColor);
	}

	@Override
	public Class<Wolf> getType() {
		return Wolf.class;
	}

	@Override
	protected int hashCode_i() {
		int prime = 31, result = 1;
		result = prime * result + angry;
		result = prime * result + tamed;
		result = prime * result + (collarColor == null ? 0 : collarColor.hashCode());
		return result;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof WolfData))
			return false;
		WolfData other = (WolfData) obj;
		if (angry != other.angry)
			return false;
		if (tamed != other.tamed)
			return false;
		if (collarColor != other.collarColor)
			return false;
		return true;
	}

	/**
	 * Note that this method is only used when changing Skript versions 2.1 to anything above.
	 */
	@Deprecated
	@Override
	protected boolean deserialize(String s) {
		String[] split = s.split("\\|");
		if (split.length != 2)
			return false;
		try {
			angry = Integer.parseInt(split[0]);
			tamed = Integer.parseInt(split[1]);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (entityData instanceof WolfData) {
			WolfData wolfData = (WolfData) entityData;
			return (angry == 0 || wolfData.angry == angry) && (tamed == 0 || wolfData.tamed == tamed) && (wolfData.collarColor == collarColor);
		}
		return false;
	}

	@Override
	public EntityData<Wolf> getSuperType() {
		return new WolfData();
	}

}
