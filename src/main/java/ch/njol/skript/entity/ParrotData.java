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

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class ParrotData extends EntityData<Parrot> {
	
	// Cached variants, values() copies array each time it is called
	@SuppressWarnings("null") // If null, ParrotData is not registered
	private static Object[] variants;
	
	static {
		if (Skript.classExists("org.bukkit.entity.Parrot")) {
			variants = Parrot.Variant.values();
			EntityData.register(ParrotData.class, "parrot", Parrot.class, 0,
					"parrot", "red parrot", "blue parrot", "green parrot", "cyan parrot", "gray parrot");
		}
	}
	
	/**
	 * Parrot variant. To avoid literally crashing on MC<1.12,
	 * we just map enum values to int.
	 */
	private int variant;
	
	public ParrotData() {}
	
	public ParrotData(int variant) {
		this.variant = variant;
		super.matchedPattern = variant + 1;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern == 0) { // Just a parrot, variant -1 to request random variant on spawn
			variant = -1;
		} else { // Specific variant was given
			variant = matchedPattern - 1; // Patterns begin from 1, enum elements from 0
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Parrot> c, @Nullable Parrot e) {
		if (e != null) { // Entity provided, take its variant
			Parrot.Variant entityVariant = e.getVariant();
			for (int i = 0; i < variants.length; i++) {
				Object var = variants[i];
				if (var == entityVariant) { // Found variant of entity
					variant = i; // Put its numeric id here
				}
			}
		} else { // No entity, ask for variant randomization on spawn
			variant = -1;
		}
		return true;
	}

	@Override
	public void set(Parrot entity) {
		if (variant == -1) {
			int randomVariant = ThreadLocalRandom.current().nextInt(0, variants.length);
			entity.setVariant((Variant) variants[randomVariant]);
		} else {
			entity.setVariant((Variant) variants[variant]);
		}
	}

	@Override
	protected boolean match(Parrot entity) {
		return variant == -1 || entity.getVariant() == variants[variant];
	}

	@Override
	public Class<? extends Parrot> getType() {
		return Parrot.class;
	}

	@Override
	public EntityData getSuperType() {
		return new ParrotData(variant);
	}

	@Override
	protected int hashCode_i() {
		return variant;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (obj instanceof ParrotData)
			return ((ParrotData) obj).variant == variant;
		return false;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		// True if e is parrot, and this is generic parrot OR if this and e are similar parrots
		return e instanceof ParrotData && (variant == -1 || ((ParrotData) e).variant == variant);
	}
	
}
