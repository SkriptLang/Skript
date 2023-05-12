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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
public class VillagerData extends EntityData<Villager> {
	
	/**
	 * Professions can be for zombies also. These are the ones which are only
	 * for villagers.
	 */
	private static List<Profession> professions;

	static {
		// professions in order!
		// NORMAL(-1), FARMER(0), LIBRARIAN(1), PRIEST(2), BLACKSMITH(3), BUTCHER(4), NITWIT(5);
		
		Variables.yggdrasil.registerSingleClass(Profession.class, "Villager.Profession");
		
		if (Skript.isRunningMinecraft(1, 14)) {
			EntityData.register(VillagerData.class, "villager", Villager.class, 0,
					"villager", "normal", "armorer", "butcher", "cartographer",
					"cleric", "farmer", "fisherman", "fletcher",
					"leatherworker", "librarian", "mason", "nitwit",
					"shepherd", "toolsmith", "weaponsmith");
			professions = Arrays.asList(Profession.values());
		} else { // Post 1.10: Not all professions go for villagers
			EntityData.register(VillagerData.class, "villager", Villager.class, 0,
					"normal", "villager", "farmer", "librarian",
					"priest", "blacksmith", "butcher", "nitwit");
			// Normal is for zombie villagers, but needs to be here, since someone thought changing first element in enum was good idea :(

			professions = new ArrayList<>();
			for (Profession prof : Profession.values()) {
				// We're better off doing stringfying the constants since these don't exist in 1.14
				if (!prof.toString().equals("NORMAL") && !prof.toString().equals("HUSK"))
					professions.add(prof);
			}
		}
	}
	
	@Nullable
	private Profession profession = null;
	
	public VillagerData() {}
	
	public VillagerData(@Nullable Profession profession) {
		this.profession = profession;
		this.matchedPattern = profession != null ? professions.indexOf(profession) + 1 : 0;
	}
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (matchedPattern > 0)
			profession = professions.get(matchedPattern - 1);
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Villager> c, final @Nullable Villager e) {
		profession = e == null ? null : e.getProfession();
		return true;
	}
	
	@Override
	public void set(final Villager entity) {
		Profession prof = profession == null ? CollectionUtils.getRandom(professions) : profession;
		assert prof != null;
		entity.setProfession(prof);
		if (profession == Profession.NITWIT)
			entity.setRecipes(Collections.emptyList());
	}
	
	@Override
	protected boolean match(final Villager entity) {
		return profession == null || entity.getProfession() == profession;
	}
	
	@Override
	public Class<? extends Villager> getType() {
		return Villager.class;
	}
	
	@Override
	protected int hashCode_i() {
		return profession != null ? profession.hashCode() : 0;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof VillagerData))
			return false;
		final VillagerData other = (VillagerData) obj;
		return profession == other.profession;
	}
	
//		return profession == null ? "" : profession.name();
	@Override
	protected boolean deserialize(final String s) {
		if (s.isEmpty())
			return true;
		try {
			profession = Profession.valueOf(s);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof VillagerData)
			return profession == null || ((VillagerData) e).profession == profession;
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new VillagerData(profession);
	}
	
}
