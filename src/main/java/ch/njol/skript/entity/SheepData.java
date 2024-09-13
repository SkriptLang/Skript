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

import org.bukkit.entity.Sheep;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
public class SheepData extends EntityData<Sheep> {
	static {
		EntityData.register(SheepData.class, "sheep", Sheep.class, 1, "unsheared sheep", "sheep", "sheared sheep");
	}
	
	@Nullable
	private Color[] colors;
	private int sheared = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		sheared = matchedPattern - 1;
		if (exprs[0] != null)
			colors = ((Literal<Color>) exprs[0]).getAll();
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(@Nullable Class<? extends Sheep> c, @Nullable Sheep e) {
		if (e != null) {
			sheared = e.isSheared() ? 1 : -1;
			colors = CollectionUtils.array(SkriptColor.fromDyeColor(e.getColor()));
		}
		return true;
	}
	
	@Override
	public void set(final Sheep entity) {
		if (colors != null) {
			final Color c = CollectionUtils.getRandom(colors);
			assert c != null;
			entity.setColor(c.asDyeColor());
		}
	}
	
	@Override
	public boolean match(final Sheep entity) {
		return (sheared == 0 || entity.isSheared() == (sheared == 1))
				&& (colors == null || SimpleExpression.check(colors, c -> entity.getColor() == c.asDyeColor(), false, false));
	}
	
	@Override
	public Class<Sheep> getType() {
		return Sheep.class;
	}
	
	@Nullable
	private Adjective[] adjectives = null;
	
	@Override
	public String toString(final int flags) {
		final Color[] colors = this.colors;
		if (colors == null)
			return super.toString(flags);
		Adjective[] adjectives = this.adjectives;
		if (adjectives == null) {
			this.adjectives = adjectives = new Adjective[colors.length];
			for (int i = 0; i < colors.length; i++)
				if (colors[i] instanceof SkriptColor)
					adjectives[i] = ((SkriptColor)colors[i]).getAdjective();
		}
		final Noun name = getName();
		final Adjective age = getAgeAdjective();
		return name.getArticleWithSpace(flags) + (age == null ? "" : age.toString(name.getGender(), flags) + " ")
				+ Adjective.toString(adjectives, name.getGender(), flags, false) + " " + name.toString(flags & Language.NO_ARTICLE_MASK);
	}
	
	@Override
	protected int hashCode_i() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(colors);
		result = prime * result + sheared;
		return result;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof SheepData))
			return false;
		final SheepData other = (SheepData) obj;
		if (!Arrays.equals(colors, other.colors))
			return false;
		if (sheared != other.sheared)
			return false;
		return true;
	}
	
//		if (colors != null) {
//			final StringBuilder b = new StringBuilder();
//			b.append(sheared);
//			b.append("|");
//			for (final Color c : colors) {
//				if (b.length() != 0)
//					b.append(",");
//				b.append(c.name());
//			}
//			return b.toString();
//		} else {
//			return "" + sheared;
//		}
	@Override
	protected boolean deserialize(final String s) {
		final String[] split = s.split("\\|");
		final String sh;
		if (split.length == 1) {
			sh = s;
		} else if (split.length == 2) {
			sh = split[0];
			final String[] cs = split[1].split(",");
			colors = new Color[cs.length];
			for (int i = 0; i < cs.length; i++) {
				try {
					final String c = cs[i];
					assert c != null;
					assert colors != null;
					colors[i] = SkriptColor.valueOf(c);
				} catch (final IllegalArgumentException e) {
					return false;
				}
			}
		} else {
			return false;
		}
		try {
			sheared = Integer.parseInt(sh);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof SheepData)
			return colors == null || CollectionUtils.isSubset(colors, ((SheepData) e).colors);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new SheepData();
	}
	
}
