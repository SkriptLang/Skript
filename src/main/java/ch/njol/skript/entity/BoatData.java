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

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.Boat;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class BoatData extends EntityData<Boat> {

	private static ItemType mangroveBoat;
	private static ItemType bambooBoat;
	private static ItemType cherryBoat;

	static {
		List<String> boats = Lists.newArrayList("spruce boat", "birch boat", "jungle boat", "acacia boat", "dark oak boat");
		if (exists("MANGROVE")) {
			boats.add("mangrove boat");
			mangroveBoat = Aliases.javaItemType("mangrove boat");
		}
		if (exists("BAMBOO")) {
			boats.add("bamboo raft");
			bambooBoat = Aliases.javaItemType("bamboo raft");
		}
		if (exists("CHERRY")) {
			boats.add("cherry boat");
			cherryBoat = Aliases.javaItemType("cherry boat");
		}
		// Spigot sorts the enum of Boat.Type alphabetically.
		Collections.sort(boats);
		// 0 index is random.
		boats.add("boat");
		EntityData.register(BoatData.class, "boat", Boat.class, 0, boats.toArray(new String[0]));
	}

	public BoatData() {
		this(0);
	}

	public BoatData(Boat.Type type) {
		this(type != null ? type.ordinal() + 1 : 1);
	}

	private BoatData(int type) {
		matchedPattern = type;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Boat> c, @Nullable Boat boat) {
		if (boat != null)
			matchedPattern = boat.getBoatType().ordinal() + 1;
		return true;
	}

	@Override
	public void set(Boat entity) {
		if (matchedPattern == 0) { // If the type is 'any boat'.
			matchedPattern += new Random().nextInt(Boat.Type.values().length); // It will spawn a random boat type in case is 'any boat'.
		} else { 
			entity.setBoatType(Boat.Type.values()[matchedPattern - 1]);
		}
	}

	@Override
	protected boolean match(Boat entity) {
		return matchedPattern == 0 || entity.getBoatType().ordinal() == matchedPattern - 1;
	}

	@Override
	public Class<? extends Boat> getType() {
		return Boat.class;
	}

	@Override
	public EntityData<?> getSuperType() {
		return new BoatData(matchedPattern);
	}

	@Override
	protected int hashCode_i() {
		return matchedPattern == 0 ? 0 : matchedPattern + 1;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (obj instanceof BoatData)
			return matchedPattern == ((BoatData)obj).matchedPattern;
		return false;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (data instanceof BoatData)
			return matchedPattern == 0 || matchedPattern == ((BoatData)data).matchedPattern;
		return false;
	}

	private static final ItemType oakBoat = Aliases.javaItemType("oak boat");
	private static final ItemType spruceBoat = Aliases.javaItemType("spruce boat");
	private static final ItemType birchBoat = Aliases.javaItemType("birch boat");
	private static final ItemType jungleBoat = Aliases.javaItemType("jungle boat");
	private static final ItemType acaciaBoat = Aliases.javaItemType("acacia boat");
	private static final ItemType darkOakBoat = Aliases.javaItemType("dark oak boat");

	public boolean isOfItemType(ItemType itemType) {
		if (itemType.getRandom() == null)
			return false;
		int ordinal = -1;

		ItemStack stack = itemType.getRandom();
		if (oakBoat.isOfType(stack))
			ordinal = Boat.Type.OAK.ordinal();
		else if (spruceBoat.isOfType(stack))
			ordinal = Boat.Type.SPRUCE.ordinal();
		else if (birchBoat.isOfType(stack))
			ordinal = Boat.Type.BIRCH.ordinal();
		else if (jungleBoat.isOfType(stack))
			ordinal = Boat.Type.JUNGLE.ordinal();
		else if (acaciaBoat.isOfType(stack))
			ordinal = Boat.Type.ACACIA.ordinal();
		else if (darkOakBoat.isOfType(stack))
			ordinal = Boat.Type.DARK_OAK.ordinal();
		else if (mangroveBoat.isOfType(stack) && exists("MANGROVE"))
			ordinal = Boat.Type.MANGROVE.ordinal();
		else if (bambooBoat.isOfType(stack) && exists("BAMBOO"))
			ordinal = Boat.Type.BAMBOO.ordinal();
		else if (cherryBoat.isOfType(stack) && exists("CHERRY"))
			ordinal = Boat.Type.CHERRY.ordinal();
		return hashCode_i() == ordinal + 1 || (matchedPattern + ordinal == 0) || ordinal == 0;
	}

	private static boolean exists(String string) {
		try {
			return Boat.Type.valueOf(string) != null;
		} catch (Exception ignored) {}
		return false;
	}

}
