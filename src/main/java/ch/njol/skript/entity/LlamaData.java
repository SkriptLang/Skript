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

import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.TraderLlama;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;

public class LlamaData extends EntityData<Llama> {
	
	private final static boolean TRADER_SUPPORT = Skript.classExists("org.bukkit.entity.TraderLlama");
	static {
		if (TRADER_SUPPORT)
			EntityData.register(LlamaData.class, "llama", Llama.class, 0,
					"llama", "creamy llama", "white llama", "brown llama", "gray llama",
				"trader llama", "creamy trader llama", "white trader llama", "brown trader llama", "gray trader llama");
		else if (Skript.classExists("org.bukkit.entity.Llama"))
			EntityData.register(LlamaData.class, "llama", Llama.class, 0,
					"llama", "creamy llama",
					"white llama", "brown llama", "gray llama");
	}
	
	@Nullable
	private Color color = null;
	private boolean isTrader;
	
	public LlamaData() {}
	
	public LlamaData(@Nullable Color color, boolean isTrader) {
		this.color = color;
		this.isTrader = isTrader;
		super.matchedPattern = (color != null ? (color.ordinal() + 1) : 0) + (isTrader ? 5 : 0);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		isTrader = TRADER_SUPPORT && matchedPattern > 4;
		if (TRADER_SUPPORT && matchedPattern > 5) {
			color = Color.values()[matchedPattern - 6];
		} else if (matchedPattern > 0 && matchedPattern < 5) {
			color = Color.values()[matchedPattern - 1];
		}
		
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Llama> c, @Nullable Llama llama) {
		if (TRADER_SUPPORT && c != null)
			isTrader = c.isAssignableFrom(TraderLlama.class);
		if (llama != null) {
			color = llama.getColor();
			isTrader = TRADER_SUPPORT && llama instanceof TraderLlama;
		}
		return true;
	}
	
	@Override
	public void set(Llama entity) {
		Color randomColor = color == null ? CollectionUtils.getRandom(Color.values()) : color;
		assert randomColor != null;
		entity.setColor(randomColor);
	}
	
	@Override
	protected boolean match(Llama entity) {
		return (TRADER_SUPPORT && isTrader == entity instanceof TraderLlama && (color == null || color == entity.getColor()))
			|| color == null || color == entity.getColor();
	}
	
	@Override
	public Class<? extends Llama> getType() {
		// If TraderLlama does not exist, this would ALWAYS throw ClassNotFoundException
		// (no matter if isTrader == false)
		if (TRADER_SUPPORT)
			return isTrader ? TraderLlama.class : Llama.class;
		assert !isTrader; // Shouldn't be possible on this version
		return Llama.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new LlamaData(color, isTrader);
	}
	
	@Override
	protected int hashCode_i() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (color != null ? color.hashCode() : 0);
		result = prime * result + (isTrader ? 1 : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof LlamaData))
			return false;
		LlamaData d = (LlamaData) data;
		return isTrader == d.isTrader && d.color == color;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof LlamaData))
			return false;
		LlamaData d = (LlamaData) data;
		return isTrader == d.isTrader && (color == null || d.color == color);
	}
	
}
