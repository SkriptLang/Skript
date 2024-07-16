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

import org.bukkit.Rotation;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;

public class ItemFrameData extends EntityData<ItemFrame> {

	static {
		EntityData.register(ItemFrameData.class, "item frame", ItemFrame.class, "item frame");
	}

	@Nullable
	private ItemType type;

	private Rotation rotation = Rotation.NONE;

	public ItemFrameData() {}

	public ItemFrameData(@Nullable ItemType type, @Nullable Rotation rotation) {
		this.rotation = rotation;
		this.type = type;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null)
			type = (ItemType) exprs[0].getSingle();
		if (exprs[1] != null)
			rotation = (Rotation) exprs[1].getSingle();
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends ItemFrame> c, @Nullable ItemFrame itemframe) {
		if (itemframe != null) {
			ItemStack item = itemframe.getItem();
			type = new ItemType(item);
			rotation = itemframe.getRotation();
		}
		return true;
	}

	@Override
	protected boolean match(ItemFrame itemframe) {
		if (type == null)
			return true;
		return type.isOfType(itemframe.getItem()) && itemframe.getRotation() == rotation;
	}

	@Override
	public void set(ItemFrame itemframe) {
		assert type != null;
		ItemStack item = type.getItem().getRandom();
		if (item != null)
			itemframe.setItem(item);
		itemframe.setRotation(rotation);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof ItemFrameData))
			return false;
		ItemFrameData itemFrameData = (ItemFrameData) entityData;
		if (type != null)
			return itemFrameData.type != null && type.equals(itemFrameData.type) && rotation == itemFrameData.rotation;
		return true;
	}

	@Override
	public Class<? extends ItemFrame> getType() {
		return ItemFrame.class;
	}

	@Override
	public EntityData<?> getSuperType() {
		return new ItemFrameData(type, rotation);
	}

	@Override
	public String toString(int flags) {
		if (type == null)
			return super.toString(flags);
		StringBuilder builder = new StringBuilder();
		builder.append(Noun.getArticleWithSpace(type.getTypes().get(0).getGender(), flags));
		builder.append("item frame " + type == null ? "" : "of " + type.toString(flags));
		builder.append("rotated " + Classes.toString(rotation));
		return builder.toString();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof ItemFrameData))
			return false;
		return type == null ? true : type.equals(((ItemFrameData) entityData).type) && rotation == ((ItemFrameData) entityData).rotation;
	}

	@Override
	protected int hashCode_i() {
		int prime = 31;
		int result = 1;
		result = prime * result + (type == null ? 0 : type.hashCode());
		result = prime * result + rotation.hashCode();
		return result;
	}

}
