/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.entity;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

/**
 * @author Peter Güttinger
 */
public class DroppedItemData extends EntityData<Item> {
	static {
		EntityData.register(DroppedItemData.class, "dropped item", Item.class, "dropped item");
	}

	private final static Adjective m_adjective = new Adjective("entities.dropped item.adjective");

	@Nullable
	private ItemType[] types;

	public DroppedItemData() {
	}

	public DroppedItemData(@Nullable ItemType[] types) {
		this.types = types;
	}

	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null)
			types = (ItemType[]) exprs[0].getAll();
		return true;
	}

	@Override
	protected boolean init(final @Nullable Class<? extends Item> c, final @Nullable Item e) {
		if (e != null) {
			final ItemStack i = e.getItemStack();
			types = new ItemType[]{new ItemType(i)};
		}
		return true;
	}

	@Override
	protected boolean match(final Item entity) {
		if (types != null) {
			for (final ItemType t : types) {
				if (t.isOfType(entity.getItemStack()))
					return true;
			}
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void set(final Item entity) {
		final ItemType t = CollectionUtils.getRandom(types);
		assert t != null;
		ItemStack stack = t.getItem().getRandom();
		if (stack != null)
			entity.setItemStack(stack);
	}

	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (!(e instanceof DroppedItemData))
			return false;
		final DroppedItemData d = (DroppedItemData) e;
		if (types != null)
			return d.types != null && ItemType.isSubset(types, d.types);
		return true;
	}

	@Override
	public Class<? extends Item> getType() {
		return Item.class;
	}

	@Override
	public EntityData getSuperType() {
		return new DroppedItemData(types);
	}

	@Override
	public String toString(final int flags) {
		final ItemType[] types = this.types;
		if (types == null)
			return super.toString(flags);
		final StringBuilder b = new StringBuilder();
		b.append(Noun.getArticleWithSpace(types[0].getTypes().get(0).getGender(), flags));
		b.append(m_adjective.toString(types[0].getTypes().get(0).getGender(), flags));
		b.append(" ");
		b.append(Classes.toString(types, flags & Language.NO_ARTICLE_MASK, false));
		return "" + b.toString();
	}

	//		return ItemType.serialize(types);
	@Override
	@Deprecated
	protected boolean deserialize(final String s) {
		throw new UnsupportedOperationException("old serialization is no longer supported");
//		if (s.isEmpty())
//			return true;
//		types = ItemType.deserialize(s);
//		return types != null;
	}

	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof DroppedItemData))
			return false;
		return Arrays.equals(types, ((DroppedItemData) obj).types);
	}

	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(types);
	}

}
