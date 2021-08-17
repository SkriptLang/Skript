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

import org.bukkit.entity.Cat;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;

public class CatData extends EntityData<Cat> {
	
	static {
		if (Skript.classExists("org.bukkit.entity.Cat"))
			EntityData.register(CatData.class, "cat", Cat.class, "cat");
	}
	
	private Cat.@Nullable Type race = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null)
			race = ((Literal<Cat.Type>) exprs[0]).getSingle();
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Cat> c, @Nullable Cat cat) {
		race = (cat == null) ? Cat.Type.TABBY : cat.getCatType();
		return true;
	}
	
	@Override
	public void set(Cat entity) {
		Cat.Type type = race != null ? race : CollectionUtils.getRandom(Cat.Type.values());
		assert type != null;
		entity.setCatType(type);
	}
	
	@Override
	protected boolean match(Cat entity) {
		return race == null || entity.getCatType() == race;
	}
	
	@Override
	public Class<? extends Cat> getType() {
		return Cat.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new CatData();
	}
	
	@Override
	protected int hashCode_i() {
		return race != null ? race.hashCode() : 0;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		return data instanceof CatData ? race == ((CatData) data).race : false;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		return data instanceof CatData ? race == null || race == ((CatData) data).race : false;
	}
}
