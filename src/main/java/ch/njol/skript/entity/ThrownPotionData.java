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

import java.util.Arrays;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.util.coll.CollectionUtils;

public class ThrownPotionData extends EntityData<ThrownPotion> {
	static {
		EntityData.register(ThrownPotionData.class, "thrown potion", ThrownPotion.class, "thrown potion");
	}
	
	private static final Adjective m_adjective = new Adjective("entities.thrown potion.adjective");
	private static final boolean LINGERING_POTION_ENTITY_USED = !Skript.isRunningMinecraft(1, 14);
	// LingeringPotion class deprecated and marked for removal
	@SuppressWarnings("removal")
	private static final Class<? extends ThrownPotion> LINGERING_POTION_ENTITY_CLASS =
		LINGERING_POTION_ENTITY_USED ? LingeringPotion.class : ThrownPotion.class;
	private static final Material POTION = Material.POTION;
	private static final Material SPLASH_POTION = Material.SPLASH_POTION;
	private static final Material LINGER_POTION = Material.LINGERING_POTION;
	
	@Nullable
	private ItemType[] types;
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null) {
			return (types = Converters.convert((ItemType[]) exprs[0].getAll(), ItemType.class, t -> {
				// If the itemtype is a potion, lets make it a splash potion (required by Bukkit)
				if (t.getMaterial() == POTION) {
					ItemMeta meta = t.getItemMeta();
					ItemType itemType = new ItemType(SPLASH_POTION);
					itemType.setItemMeta(meta);
					return itemType;
				} else if (t.getMaterial() != SPLASH_POTION && t.getMaterial() != LINGER_POTION) {
					return null;
				}
				return t;
			})).length != 0; // no error message - other things can be thrown as well
		} else {
			types = new ItemType[]{new ItemType(SPLASH_POTION)};
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends ThrownPotion> c, @Nullable ThrownPotion e) {
		if (e != null) {
			ItemStack i = e.getItem();
			types = new ItemType[] {new ItemType(i)};
		}
		return true;
	}
	
	@Override
	protected boolean match(ThrownPotion entity) {
		if (types != null) {
			for (ItemType t : types) {
				if (t.isOfType(entity.getItem()))
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public @Nullable ThrownPotion spawn(Location location, @Nullable Consumer<ThrownPotion> consumer) {
		ItemType t = CollectionUtils.getRandom(types);
		assert t != null;
		ItemStack i = t.getRandom();
		if (i == null)
			return null;

		Class<ThrownPotion> thrownPotionClass = (Class) (i.getType() == LINGER_POTION ? LINGERING_POTION_ENTITY_CLASS : ThrownPotion.class);
		ThrownPotion potion;
		if (consumer != null) {
			potion = EntityData.spawn(location, thrownPotionClass, consumer);
		} else {
			potion = location.getWorld().spawn(location, thrownPotionClass);
		}

		if (potion == null)
			return null;
		potion.setItem(i);
		return potion;
	}

	@Override
	public void set(ThrownPotion entity) {
		if (types != null) {
			ItemType t = CollectionUtils.getRandom(types);
			assert t != null;
			ItemStack i = t.getRandom();
			if (i == null)
				return; // Missing item, can't make thrown potion of it
			if (LINGERING_POTION_ENTITY_USED && (LINGERING_POTION_ENTITY_CLASS.isInstance(entity) != (LINGER_POTION == i.getType())))
				return;
			entity.setItem(i);
		}
		assert false;
	}
	
	@Override
	public Class<? extends ThrownPotion> getType() {
		return ThrownPotion.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new ThrownPotionData();
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		if (!(e instanceof ThrownPotionData))
			return false;
		ThrownPotionData d = (ThrownPotionData) e;
		if (types != null) {
			return d.types != null && ItemType.isSubset(types, d.types);
		}
		return true;
	}
	
	@Override
	public String toString(int flags) {
		ItemType[] types = this.types;
		if (types == null)
			return super.toString(flags);
		StringBuilder b = new StringBuilder();
		b.append(Noun.getArticleWithSpace(types[0].getTypes().get(0).getGender(), flags));
		b.append(m_adjective.toString(types[0].getTypes().get(0).getGender(), flags));
		b.append(" ");
		b.append(Classes.toString(types, flags & Language.NO_ARTICLE_MASK, false));
		return "" + b.toString();
	}
	
	//		return ItemType.serialize(types);
	@Override
	@Deprecated
	protected boolean deserialize(String s) {
		throw new UnsupportedOperationException("old serialization is no longer supported");
//		if (s.isEmpty())
//			return true;
//		types = ItemType.deserialize(s);
//		return types != null;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof ThrownPotionData))
			return false;
		return Arrays.equals(types, ((ThrownPotionData) obj).types);
	}
	
	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(types);
	}
	
}
