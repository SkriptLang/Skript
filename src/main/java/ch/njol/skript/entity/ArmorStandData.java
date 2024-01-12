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

import org.bukkit.entity.ArmorStand;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class ArmorStandData extends EntityData<ArmorStand> {

	static {
		// [:small] [:marker] [:invisible] armo[u]r stand(|1¦s) [with[noArms:out [any]] arms] [[and] with[noPlate:out] [a] base plate]
		EntityData.register(ArmorStandData.class, "armor stand", ArmorStand.class, 0, "armor stand");
	}

	private Boolean small, marker;
	private Boolean visible;
	private Boolean plate;
	private Boolean arms;

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		marker = parseResult.hasTag("marker");
		small = parseResult.hasTag("small");

		// Negate
		visible = !parseResult.hasTag("invisible");
		plate = !parseResult.hasTag("noPlate");
		arms = !parseResult.hasTag("noArms");
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends ArmorStand> c, @Nullable ArmorStand armorStand) {
		if (armorStand == null)
			return true;
		plate = armorStand.hasBasePlate();
		visible = armorStand.isVisible();
		marker = armorStand.isMarker();
		small = armorStand.isSmall();
		arms = armorStand.hasArms();
		return true;
	}

	@Override
	public void set(ArmorStand armorStand) {
		armorStand.setBasePlate(plate);
		armorStand.setVisible(visible);
		armorStand.setMarker(marker);
		armorStand.setSmall(small);
		armorStand.setArms(arms);
	}

	@Override
	protected boolean match(ArmorStand armorStand) {
		if (plate != null) {
			if (!(plate && armorStand.hasBasePlate()))
				return false;
		}
		if (visible != null) {
			if (!(visible && armorStand.isVisible()))
				return false;
		}
		if (marker != null) {
			if (!(marker && armorStand.isMarker()))
				return false;
		}
		if (small != null) {
			if (!(small && armorStand.isSmall()))
				return false;
		}
		if (arms != null) {
			if (!(arms && armorStand.hasArms()))
				return false;
		}
		return true;
	}

	@Override
	public Class<? extends ArmorStand> getType() {
		return ArmorStand.class;
	}

	@Override
	public EntityData<?> getSuperType() {
		return new ArmorStandData();
	}

	@Override
	protected int hashCode_i() {
		int prime = 31;
		int result = 1;
		if (plate != null)
			result = prime * result + Boolean.hashCode(plate);
		if (visible != null)
			result = prime * result + Boolean.hashCode(visible);
		if (marker != null)
			result = prime * result + Boolean.hashCode(marker);
		if (small != null)
			result = prime * result + Boolean.hashCode(small);
		if (arms != null)
			result = prime * result + Boolean.hashCode(arms);
		return result;
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof ArmorStandData))
			return false;
		ArmorStandData other = (ArmorStandData) entityData;
		if (!(plate != null && plate && other.plate != null && other.plate))
			return false;
		if (!(visible != null && visible && other.visible != null && other.visible))
			return false;
		if (!(marker != null && marker && other.marker != null && other.marker))
			return false;
		if (!(small != null && small && other.small != null && other.small))
			return false;
		if (!(arms != null && arms && other.arms != null && other.arms))
			return false;
		return true;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (entityData instanceof ArmorStandData)
			return true;
		return entityData.getType().isAssignableFrom(ArmorStand.class);
	}

}
