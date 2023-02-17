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

import org.bukkit.entity.Entity;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public class EntityType implements Cloneable, YggdrasilSerializable {
	
	static {
		Classes.registerClass(new ClassInfo<>(EntityType.class, "entitytype")
				.name("Entity Type with Amount")
				.description("An <a href='#entitydata'>entity type</a> with an amount, e.g. '2 zombies'. I might remove this type in the future and make a more general 'type' type, i.e. a type that has a number and a type.")
				.usage("&lt;<a href='#number'>number</a>&gt; &lt;entity type&gt;")
				.examples("spawn 5 creepers behind the player")
				.since("1.3")
				.defaultExpression(new SimpleLiteral<>(new EntityType(Entity.class, 1), true))
				.parser(new Parser<EntityType>() {
					@Override
					@Nullable
					public EntityType parse(final String s, final ParseContext context) {
						return EntityType.parse(s);
					}
					
					@Override
					public String toString(final EntityType t, final int flags) {
						return t.toString(flags);
					}
					
					@Override
					public String toVariableNameString(final EntityType t) {
						return "entitytype:" + t.toString();
					}
                })
				.serializer(new YggdrasilSerializer<>()));
	}
	
	public int amount = -1;
	
	public final EntityData<?> data;
	
	/**
	 * Only used for deserialisation
	 */
	@SuppressWarnings({"unused", "null"})
	private EntityType() {
		data = null;
	}
	
	public EntityType(final EntityData<?> data, final int amount) {
		assert data != null;
		this.data = data;
		this.amount = amount;
	}
	
	public EntityType(final Class<? extends Entity> c, final int amount) {
		assert c != null;
		data = EntityData.fromClass(c);
		this.amount = amount;
	}
	
	public EntityType(final Entity e) {
		data = EntityData.fromEntity(e);
	}
	
	public EntityType(final EntityType other) {
		amount = other.amount;
		data = other.data;
	}
	
	public boolean isInstance(final Entity entity) {
		return data.isInstance(entity);
	}
	
	@Override
	public String toString() {
		return getAmount() == 1 ? data.toString(0) : amount + " " + data.toString(Language.F_PLURAL);
	}
	
	public String toString(final int flags) {
		return getAmount() == 1 ? data.toString(flags) : amount + " " + data.toString(flags | Language.F_PLURAL);
	}
	
	public int getAmount() {
		return amount == -1 ? 1 : amount;
	}
	
	public boolean sameType(final EntityType other) {
		return data.equals(other.data);
	}
	
	@SuppressWarnings("null")
	@Nullable
	public static EntityType parse(String s) {
		assert s != null && s.length() != 0;
		int amount = -1;
		if (s.matches("\\d+ .+")) {
			amount = Utils.parseInt(s.split(" ", 2)[0]);
			s = s.split(" ", 2)[1];
		} else if (s.matches("(?i)an? .+")) {
			s = s.split(" ", 2)[1];
		}
//		final Pair<String, Boolean> p = Utils.getPlural(s, amount != 1 && amount != -1);
//		s = p.first;
		final EntityData<?> data = EntityData.parseWithoutIndefiniteArticle(s);
		if (data == null)
			return null;
		return new EntityType(data, amount);
	}
	
	@Override
	public EntityType clone() {
		return new EntityType(this);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + data.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EntityType))
			return false;
		final EntityType other = (EntityType) obj;
		if (amount != other.amount)
			return false;
		if (!data.equals(other.data))
			return false;
		return true;
	}
	
}
