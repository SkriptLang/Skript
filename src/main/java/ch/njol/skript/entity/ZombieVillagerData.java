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

import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class ZombieVillagerData extends EntityData<ZombieVillager> {

	private final static boolean PROFESSION_UPDATE = Skript.isRunningMinecraft(1, 14);
	private final static Villager.Profession[] professions = Villager.Profession.values();

	static {
		if (PROFESSION_UPDATE)
			EntityData.register(ZombieVillagerData.class, "zombie villager", ZombieVillager.class, 0,
				"zombie villager", "zombie armorer", "zombie butcher", "zombie cartographer", "zombie cleric", "zombie farmer", "zombie fisherman",
				"zombie fletcher", "zombie leatherworker", "zombie librarian", "zombie mason", "zombie nitwit", "zombie shepherd", "zombie toolsmith", "zombie weaponsmith");
		else
			EntityData.register(ZombieVillagerData.class, "zombie villager", ZombieVillager.class, 0,
					"zombie villager", "zombie farmer", "zombie librarian", "zombie priest", "zombie blacksmith", "zombie butcher", "zombie nitwit");
	}
	
	private Villager.Profession profession = PROFESSION_UPDATE ? Profession.NONE : Profession.valueOf("NORMAL");
	
	public ZombieVillagerData() {}
	
	public ZombieVillagerData(Profession prof) {
		profession = prof;
		super.matchedPattern = prof.ordinal();
	}

	@SuppressWarnings("null")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		profession = professions[matchedPattern];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(final @Nullable Class<? extends ZombieVillager> c, final @Nullable ZombieVillager e) {
		if (e == null)
			return true;
		profession = e.getVillagerProfession();
		
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean deserialize(final String s) {
		try {
			profession = professions[Integer.parseInt(s)];
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			throw new SkriptAPIException("Cannot parse zombie villager type " + s);
		}
		
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public void set(final ZombieVillager e) {
		e.setVillagerProfession(profession);
	}
	
	@Override
	protected boolean match(final ZombieVillager e) {
		return e.getVillagerProfession() == profession;
	}
	
	@Override
	public Class<? extends ZombieVillager> getType() {
		return ZombieVillager.class;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof ZombieVillagerData))
			return false;
		return ((ZombieVillagerData) obj).profession == profession;
	}
	
	@Override
	protected int hashCode_i() {
		return profession.hashCode();
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof ZombieVillagerData)
			return ((ZombieVillagerData) e).profession.equals(profession);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new ZombieVillagerData(profession);
	}
}
