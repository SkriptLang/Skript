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

import java.lang.reflect.Method;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class BoatData extends EntityData<Boat> {
	static {
		// It will only register for 1.10+,
		// See SimpleEntityData if 1.9 or lower.
		if (Skript.methodExists(Boat.class, "getWoodType")) { //The 'boat' is the same of 'oak boat', 'any boat' works as supertype and it can spawn random boat.
			EntityData.register(BoatData.class, "boat", Boat.class, 0,
					"boat", "any boat", "oak boat", "spruce boat", "birch boat", "jungle boat", "acacia boat", "dark oak boat");
		}
	}
	
	public BoatData(){
		this(0);
	}
	
	public BoatData(@Nullable TreeSpecies type){
		this(type != null ? type.ordinal() + 2 : 1);
	}
	
	private BoatData(int type){
		matchedPattern = type;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Boat> c, @Nullable Boat e) {
		if (e != null)
			matchedPattern = 2 + e.getWoodType().ordinal();
		return true;
	}

	@Override
	public void set(Boat entity) {
		if (matchedPattern == 1) // If the type is 'any boat'.
			matchedPattern += new Random().nextInt(TreeSpecies.values().length); // It will spawn a random boat type in case is 'any boat'.
		if (matchedPattern > 1) // 0 and 1 are excluded
			entity.setWoodType(TreeSpecies.values()[matchedPattern - 2]); // Removes 2 to fix the index.
	}

	@Override
	protected boolean match(Boat entity) {
		return matchedPattern <= 1 || entity.getWoodType().ordinal() == matchedPattern - 2;
	}

	@Override
	public Class<? extends Boat> getType() {
		return Boat.class;
	}

	@Override
	public EntityData getSuperType() {
		return new BoatData(matchedPattern);
	}

	@Override
	protected int hashCode_i() {
		return matchedPattern <= 1 ? 0 : matchedPattern;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (obj instanceof BoatData)
			return matchedPattern == ((BoatData)obj).matchedPattern;
		return false;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		if (e instanceof BoatData)
			return matchedPattern <= 1 || matchedPattern == ((BoatData)e).matchedPattern;
		return false;
	}
	
	public boolean isOfItemType(ItemType i){
		int ordinal = -1;

		Material type = i.getMaterial();
		if (type == Material.OAK_BOAT)
			ordinal = 0;
		else if (type == Material.SPRUCE_BOAT)
			ordinal = TreeSpecies.REDWOOD.ordinal();
		else if (type == Material.BIRCH_BOAT)
			ordinal = TreeSpecies.BIRCH.ordinal();
		else if (type == Material.JUNGLE_BOAT)
			ordinal = TreeSpecies.JUNGLE.ordinal();
		else if (type == Material.ACACIA_BOAT)
			ordinal = TreeSpecies.ACACIA.ordinal();
		else if (type == Material.DARK_OAK_BOAT)
			ordinal = TreeSpecies.DARK_OAK.ordinal();
		return hashCode_i() == ordinal + 2 || (matchedPattern + ordinal == 0) || ordinal == 0;
		
	}
}
