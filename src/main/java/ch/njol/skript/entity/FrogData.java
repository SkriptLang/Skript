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

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Frog.Variant;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FrogData extends EntityData<Frog> {

	static {
		if (Skript.classExists("org.bukkit.entity.Frog")) {
			EntityData.register(FrogData.class, "frog", Frog.class, 0,
				"frog", "temperate frog", "warm frog", "cold frog");
		}
	}

	@Nullable
	private Variant variant = null;

	public FrogData() {
	}

	public FrogData(@Nullable Variant variant) {
		this.variant = variant;
		matchedPattern = 0;
		if (variant == Variant.TEMPERATE)
			matchedPattern = 1;
		if (variant == Variant.WARM)
			matchedPattern = 2;
		if (variant == Variant.COLD)
			matchedPattern = 3;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, SkriptParser.ParseResult parseResult) {
		switch (matchedPattern) {
			case 1:
				variant = Variant.TEMPERATE;
				break;
			case 2:
				variant = Variant.WARM;
				break;
			case 3:
				variant = Variant.COLD;
				break;
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Frog> c, @Nullable Frog frog) {
		if (frog != null)
			variant = frog.getVariant();
		return true;
	}

	@Override
	public void set(Frog entity) {
		if (variant != null)
			entity.setVariant(variant);
	}

	@Override
	protected boolean match(Frog entity) {
		return variant == null || variant == entity.getVariant();
	}

	@Override
	public Class<? extends Frog> getType() {
		return Frog.class;
	}

	@Override
	public EntityData getSuperType() {
		return new FrogData(variant);
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof FrogData))
			return false;
		return variant == ((FrogData) data).variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof FrogData))
			return false;
		return variant == null || variant == ((FrogData) data).variant;
	}

}
