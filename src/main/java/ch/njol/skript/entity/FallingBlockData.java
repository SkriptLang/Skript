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

import java.util.Iterator;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import org.skriptlang.skript.lang.converter.Converter;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.util.coll.CollectionUtils;

public class FallingBlockData extends EntityData<FallingBlock> {
	static {
		EntityData.register(FallingBlockData.class, "falling block", FallingBlock.class, "falling block");
	}
	
	private final static Message m_not_a_block_error = new Message("entities.falling block.not a block error");
	private final static Adjective m_adjective = new Adjective("entities.falling block.adjective");

	@Nullable
	private ItemType[] types = null;
	
	public FallingBlockData() {}
	
	public FallingBlockData(@Nullable ItemType[] types) {
		this.types = types;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null) {
			if ((types = Converters.convert(((Literal<ItemType>) exprs[0]).getAll(), ItemType.class, new Converter<ItemType, ItemType>() {
				@Override
				@Nullable
				public ItemType convert(ItemType t) {
					t = t.getBlock().clone();
					final Iterator<ItemData> iter = t.iterator();
					while (iter.hasNext()) {
						final Material id = iter.next().getType();
						if (!id.isBlock())
							iter.remove();
					}
					if (t.numTypes() == 0)
						return null;
					t.setAmount(-1);
					t.setAll(false);
					t.clearEnchantments();
					return t;
				}
			})).length == 0) {
				Skript.error(m_not_a_block_error.toString());
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends FallingBlock> c, final @Nullable FallingBlock e) {
		if (e != null) // TODO material data support
			types = new ItemType[] {new ItemType(BlockCompat.INSTANCE.fallingBlockToState(e))};
		return true;
	}
	
	@Override
	protected boolean match(final FallingBlock entity) {
		if (types != null) {
			for (final ItemType t : types) {
				if (t.isOfType(BlockCompat.INSTANCE.fallingBlockToState(entity)))
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public FallingBlock spawn(Location loc, @Nullable Consumer<FallingBlock> consumer) {
		ItemType t = types == null ? new ItemType(Material.STONE) : CollectionUtils.getRandom(types);
		assert t != null;
		Material material = t.getMaterial();
		if (!material.isBlock()) {
			assert false : t;
			return null;
		}

		FallingBlock fallingBlock = loc.getWorld().spawnFallingBlock(loc, material.createBlockData());
		if (consumer != null)
			consumer.accept(fallingBlock);

		return fallingBlock;
	}

	@Override
	public void set(final FallingBlock entity) {
		assert false;
	}
	
	@Override
	public Class<? extends FallingBlock> getType() {
		return FallingBlock.class;
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (!(e instanceof FallingBlockData))
			return false;
		final FallingBlockData d = (FallingBlockData) e;
		if (types != null) {
			if (d.types != null)
				return ItemType.isSubset(types, d.types);
			return false;
		}
		return true;
	}
	
	@Override
	public EntityData getSuperType() {
		return new FallingBlockData(types);
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
		throw new UnsupportedOperationException("old serialization is not supported");
//		if (s.isEmpty())
//			return true;
//		types = ItemType.deserialize(s);
//		return types != null;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof FallingBlockData))
			return false;
		return Arrays.equals(types, ((FallingBlockData) obj).types);
	}
	
	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(types);
	}
	
}
