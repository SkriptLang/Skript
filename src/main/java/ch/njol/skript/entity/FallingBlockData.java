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
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Consumer;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
public class FallingBlockData extends EntityData<FallingBlock> {

	static {
		EntityData.register(FallingBlockData.class, "falling block", FallingBlock.class, "falling block");
	}

	private final static Message m_not_a_block_error = new Message("entities.falling block.not a block error");
	private final static Adjective m_adjective = new Adjective("entities.falling block.adjective");

	private ItemType @Nullable [] types = null;
	private @Nullable BlockData data = null;

	public FallingBlockData() {}

	public FallingBlockData(ItemType @Nullable [] types) {
		this.types = types;
	}

	public FallingBlockData(@Nullable BlockData data) {
		if (data != null) {
			this.types = new ItemType[] {new ItemType(data.getMaterial())};
			this.data = data;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null) {
			if ((types = Converters.convert(((Literal<ItemType>) exprs[0]).getAll(), ItemType.class, itemType -> {
				itemType = itemType.getBlock().clone();
				Iterator<ItemData> iter = itemType.iterator();
				while (iter.hasNext()) {
					Material id = iter.next().getType();
					if (!id.isBlock())
						iter.remove();
				}
				if (itemType.numTypes() == 0)
					return null;
				itemType.setAmount(-1);
				itemType.setAll(false);
				itemType.clearEnchantments();
				return itemType;
			})).length == 0) {
				Skript.error(m_not_a_block_error.toString());
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends FallingBlock> c, @Nullable FallingBlock fallingBlock) {
		if (fallingBlock != null) {
			types = new ItemType[] {new ItemType(BlockCompat.INSTANCE.fallingBlockToState(fallingBlock))};
			data = fallingBlock.getBlockData();
		}
		return true;
	}

	@Override
	protected boolean match(FallingBlock entity) {
		if (types != null) {
			for (ItemType type : types) {
				if (type.isOfType(BlockCompat.INSTANCE.fallingBlockToState(entity)))
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
		FallingBlock fallingBlock = loc.getWorld().spawnFallingBlock(loc, data == null ? material.createBlockData() : data);
		if (consumer != null)
			consumer.accept(fallingBlock);

		return fallingBlock;
	}

	@Override
	public void set(FallingBlock entity) {
		assert false;
	}

	@Override
	public Class<? extends FallingBlock> getType() {
		return FallingBlock.class;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		if (!(e instanceof FallingBlockData))
			return false;
		FallingBlockData d = (FallingBlockData) e;
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
	public String toString(int flags) {
		if (types == null)
			return super.toString(flags);
		StringBuilder builder = new StringBuilder();
		builder.append(Noun.getArticleWithSpace(types[0].getTypes().get(0).getGender(), flags));
		builder.append(m_adjective.toString(types[0].getTypes().get(0).getGender(), flags));
		builder.append(" ");
		builder.append(Classes.toString(types, flags & Language.NO_ARTICLE_MASK, false));
		return builder.toString();
	}

	@Override
	@Deprecated
	protected boolean deserialize(String s) {
		throw new UnsupportedOperationException("old serialization is not supported");
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof FallingBlockData))
			return false;
		if (data != null && ((FallingBlockData) obj).data != null)
			return data.equals(((FallingBlockData) obj).data);
		return Arrays.equals(types, ((FallingBlockData) obj).types);
	}

	@Override
	protected int hashCode_i() {
		return Objects.hash(Arrays.hashCode(types), data);
	}
	
}
